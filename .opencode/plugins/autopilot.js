// Autopilot (experimental) — keeps the agent working for a time budget.
// Start:  /autopilot [min]  (or: bash scripts/autopilot.sh start 120)
// Stop:   /autopilot-stop · scripts/autopilot.sh stop "reason" · a STOP file in the root · Esc/error
// Only acts while .opencode/autopilot.state exists with active=1. Ignores child sessions.
//
// OpenCode plugin API V2: see https://opencode.ai/v2/docs/build/plugins
import { Plugin } from "@opencode/plugin"
import { existsSync, readFileSync, writeFileSync } from "node:fs"
import { join } from "node:path"

const STATE = ".opencode/autopilot.state"

function readState(dir) {
  const p = join(dir, STATE)
  if (!existsSync(p)) return null
  const st = {}
  for (const line of readFileSync(p, "utf8").split("\n")) {
    const i = line.indexOf("=")
    if (i > 0) st[line.slice(0, i).trim()] = line.slice(i + 1).trim()
  }
  return st
}

function writeState(dir, st) {
  const body = Object.entries(st).map(([k, v]) => `${k}=${v ?? ""}`).join("\n") + "\n"
  writeFileSync(join(dir, STATE), body)
}

// The event envelope differs across event kinds; read the payload defensively.
const payload = (event) => event?.data ?? event?.properties ?? {}

// Idle: `session.status` (current) or the deprecated `session.idle` (older builds).
function isIdle(event) {
  if (event?.type === "session.idle") return true
  return event?.type === "session.status" && payload(event)?.status?.type === "idle"
}

// Failure or interrupt of a run: stop on purpose instead of retrying blindly.
function isStop(event) {
  return (
    event?.type === "session.execution.failed" ||
    event?.type === "session.execution.interrupted" ||
    event?.type === "session.error" // legacy fallback
  )
}

export default Plugin.define({
  id: "autopilot",
  async setup(ctx) {
    const directory = ctx.location.directory
    let lastPromptAt = 0

    const log = (level, message) => {
      const line = `[autopilot] ${message}`
      if (level === "error") console.error(line)
      else console.log(line)
    }

    const deactivate = (st, reason) => {
      st.active = "0"
      st.reason = reason
      writeState(directory, st)
      log("info", `stopped: ${reason}`)
    }

    const send = (sessionID, text) => {
      // fire-and-forget: never block the event loop
      void Promise.resolve(ctx.session.prompt({ sessionID, text })).catch((e) =>
        log("error", `failed to send prompt: ${String(e)}`),
      )
    }

    const controller = new AbortController()
    void (async () => {
      try {
        for await (const event of ctx.event.subscribe({ signal: controller.signal })) {
          const st = readState(directory)
          if (!st || st.active !== "1") continue

          if (isStop(event)) {
            deactivate(st, "error or interruption")
            continue
          }
          if (!isIdle(event)) continue

          const sessionID = payload(event)?.sessionID
          if (!sessionID) continue

          // ignore child sessions (subagents) and unrelated sessions
          try {
            const s = await ctx.session.get({ sessionID })
            if (s?.parentID) continue
          } catch {
            // if it cannot be verified, carry on
          }
          if (st.session && st.session !== sessionID) continue
          if (!st.session) st.session = sessionID

          if (Date.now() - lastPromptAt < 3000) continue

          if (existsSync(join(directory, "STOP"))) {
            deactivate(st, "STOP file found")
            continue
          }

          const start = Number(st.start) || Date.now() / 1000
          const budget = Number(st.budget_min) || 120
          const count = Number(st.count) || 0
          const max = Number(st.max) || 60
          const elapsed = Math.floor((Date.now() / 1000 - start) / 60)
          const remaining = budget - elapsed

          // we already asked for a wrap-up and the session went idle again → finish
          if (st.wrapup === "1") {
            deactivate(st, "budget spent (wrap-up done)")
            continue
          }

          if (count >= max) {
            deactivate(st, `iteration limit of ${max}`)
            continue
          }

          lastPromptAt = Date.now()

          if (remaining <= 0) {
            st.wrapup = "1"
            st.count = String(count + 1)
            writeState(directory, st)
            send(
              sessionID,
              `[AUTOPILOT] Budget of ${budget} min spent (${elapsed} min elapsed). ` +
                `Finish what is in progress, run scripts/check.sh, update TODO.md/PROGRESS.md, commit, and ` +
                `write the final summary (skill session-handoff). Then run: bash scripts/autopilot.sh stop "budget spent".`,
            )
            continue
          }

          st.count = String(count + 1)
          writeState(directory, st)
          send(
            sessionID,
            `[AUTOPILOT ${count + 1}/${max}] ${elapsed} min elapsed, ~${remaining} min left. ` +
              `Continue the work loop (AGENTS.md §4): re-read PROGRESS.md/TODO.md/QUESTIONS.md, pick the next task and do it. ` +
              `Do not ask for confirmations. If in doubt, apply the 5 angles (skill decide-without-asking) and decide. ` +
              `If the queue is empty, follow AGENTS.md §4.4. If you are truly blocked on everything, record it in QUESTIONS.md and run ` +
              `bash scripts/autopilot.sh stop "reason". If there is no useful work left: bash scripts/autopilot.sh stop "done".`,
          )
        }
      } catch {
        // the event stream ended or was aborted during unload
      }
    })()

    return () => controller.abort()
  },
})
