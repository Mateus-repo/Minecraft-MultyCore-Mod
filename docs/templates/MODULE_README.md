# <module-name>

> <One sentence: what this module does.>

**Does not do:** <explicit limits — what falls outside this module and where it lives>.

## Public API

Entry point: `<path/index>`. Everything else is internal and **should not** be imported from outside.

| Symbol | Signature | Description | Errors |
|---------|------------|-----------|-------|
| `example()` | `example(x: T, opts?: Opts): R` | What it does, in one line | `ErrorX` if … |

## Usage example

```
// the smallest example that shows the typical case (must actually run)
```

## Dependencies

- **Allowed:** <modules/libraries it depends on>.
- **Forbidden:** <e.g.: UI, web framework, direct DB access>.

## Notes and limitations

- <relevant decisions (ref. D-NNN), edge cases, performance, thread-safety>.

## Tests

`<command to run only this module's tests>` — tests on the public API in `<path>`.
