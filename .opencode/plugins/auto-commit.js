// Auto-commit — safety net.
// When the session becomes idle and there are changes, make a "chore(wip): checkpoint" commit.
// Config: .opencode/autocommit.json  |  disable: AUTOCOMMIT=off
// Safety: never commits sensitive files, and cancels the checkpoint if the diff matches a secret pattern.
//
// OpenCode plugin API V2: see https://opencode.ai/v2/docs/build/plugins
import { Plugin } from "@opencode/plugin"
import { existsSync, readFileSync } from "node:fs"
import { join } from "node:path"
import { execFile } from "node:child_process"

const DEFAULTS = {
  enabled: true,
  push: false,
  minIntervalSeconds: 15,
  blockSecrets: true,
  message: "chore(wip): checkpoint",
}

const SENSITIVE_FILE =
  /(^|\/)(\.env(\.[^/]*)?|[^/]*\.pem|[^/]*\.p12|[^/]*\.pfx|id_rsa|id_ed25519|credentials\.json)$/i
const SAFE_ENV_FILE = /\.env\.(example|sample|template)$/i
const SECRET_PATTERNS = [
  /-----BEGIN [A-Z ]*PRIVATE KEY-----/,
  /AKIA[0-9A-Z]{16}/,
  /ghp_[A-Za-z0-9]{36,}/,
  /github_pat_[A-Za-z0-9_]{50,}/,
  /(^|[^A-Za-z0-9])sk-[A-Za-z0-9_-]{32,}/,
  /xox[baprs]-[A-Za-z0-9-]{10,}/,
]

function loadConfig(dir) {
  try {
    const p = join(dir, ".opencode", "autocommit.json")
    if (existsSync(p)) return { ...DEFAULTS, ...JSON.parse(readFileSync(p, "utf8")) }
  } catch {
    // invalid config → fall back to defaults
  }
  return { ...DEFAULTS }
}

// The event envelope differs across event kinds; read the payload defensively.
const payload = (event) => event?.data ?? event?.properties ?? {}

// Both `session.status` (current) and the deprecated `session.idle` (older builds) signal an idle session.
function isIdle(event) {
  if (event?.type === "session.idle") return true
  if (event?.type === "session.status" && payload(event)?.status?.type === "idle") return true
  return false
}

export default Plugin.define({
  id: "auto-commit",
  async setup(ctx) {
    const directory = ctx.location.directory
    let busy = false
    let lastRun = 0

    const log = (level, message) => {
      const line = `[auto-commit] ${message}`
      if (level === "error") console.error(line)
      else if (level === "warn") console.warn(line)
      else console.log(line)
    }

    const git = (...args) =>
      new Promise((resolve) => {
        execFile("git", ["-C", directory, ...args], { windowsHide: true }, (error, stdout) => {
          const code = error ? (typeof error.code === "number" ? error.code : 1) : 0
          resolve({ code, out: String(stdout ?? "") })
        })
      })

    const checkpoint = async (cfg) => {
      const inside = await git("rev-parse", "--is-inside-work-tree")
      if (inside.code !== 0) return

      // do not act in the middle of a merge/rebase/cherry-pick
      const gd = (await git("rev-parse", "--git-dir")).out.trim()
      const gitDir = gd.startsWith("/") || /^[A-Za-z]:/.test(gd) ? gd : join(directory, gd)
      for (const marker of ["MERGE_HEAD", "rebase-merge", "rebase-apply", "CHERRY_PICK_HEAD", "REVERT_HEAD"]) {
        if (existsSync(join(gitDir, marker))) return
      }

      const status = await git("status", "--porcelain")
      if (status.code !== 0 || !status.out.trim()) return

      await git("add", "-A")

      const names = (await git("diff", "--cached", "--name-only")).out
        .split("\n")
        .map((s) => s.trim())
        .filter(Boolean)

      for (const f of names) {
        if (SENSITIVE_FILE.test(f) && !SAFE_ENV_FILE.test(f)) {
          await git("reset", "-q", "HEAD", "--", f)
          log("warn", `sensitive file skipped in checkpoint: ${f}`)
        }
      }

      if (cfg.blockSecrets) {
        const diff = (await git("diff", "--cached", "-U0", "--no-color")).out
        const added = diff.split("\n").filter((l) => l.startsWith("+") && !l.startsWith("+++"))
        const hit = added.some((l) => SECRET_PATTERNS.some((re) => re.test(l)))
        if (hit) {
          await git("reset", "-q")
          log("warn", "possible secret in the diff — checkpoint canceled. Remove the secret and try again.")
          return
        }
      }

      const staged = await git("diff", "--cached", "--quiet")
      if (staged.code === 0) return // nothing staged

      const count = (await git("diff", "--cached", "--name-only")).out.split("\n").filter(Boolean).length
      const res = await git("commit", "-q", "-m", `${cfg.message} (${count} ${count === 1 ? "file" : "files"})`)
      if (res.code !== 0) {
        log("warn", "commit failed (git hooks or config?). Changes stay staged.")
        return
      }
      log("info", `checkpoint done (${count} ${count === 1 ? "file" : "files"}).`)

      if (cfg.push) {
        const p = await git("push", "-q")
        if (p.code !== 0) log("warn", "git push failed (no remote/upstream?).")
      }
    }

    const controller = new AbortController()
    void (async () => {
      try {
        for await (const event of ctx.event.subscribe({ signal: controller.signal })) {
          if (!isIdle(event)) continue
          const cfg = loadConfig(directory)
          if (!cfg.enabled || process.env.AUTOCOMMIT === "off" || busy) continue
          if (Date.now() - lastRun < cfg.minIntervalSeconds * 1000) continue
          busy = true
          try {
            await checkpoint(cfg)
          } catch (e) {
            log("error", String(e))
          } finally {
            busy = false
            lastRun = Date.now()
          }
        }
      } catch {
        // the event stream ended or was aborted during unload
      }
    })()

    return () => controller.abort()
  },
})
