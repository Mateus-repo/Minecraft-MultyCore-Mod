# thread

> Owns the mod's worker threads and identifies the server main thread.

**Does not do:** decide *what* work is offloaded (that is `TaskScheduler`, Phase 1.3), run Minecraft
code (that is the mixin layer), or store world state.

## Public API

Entry point: `thread/WorkerPool` and `thread/MainThread`. Everything else is internal.

| Symbol | Signature | Description | Errors |
|---------|------------|-----------|-------|
| `WorkerPool` | `WorkerPool(String poolName, int size)` | Named fixed pool; size `<= 0` disables it | — |
| `start()` | `void start()` | Create the threads (no-op if disabled/already running) | — |
| `isRunning()` | `boolean isRunning()` | Whether the pool accepts work | — |
| `size()` | `int size()` | Configured worker count | — |
| `submit(task)` | `boolean submit(Runnable)` | Queue a task; `false` if stopped | never throws |
| `submitAll(tasks)` | `<T> List<Future<T>> submitAll(Collection<? extends Callable<T>>)` | Queue a batch | never throws |
| `shutdown()` | `void shutdown()` | Graceful drain, then stop | never throws |
| `MainThread.capture()` | `static void capture()` | Record the server main thread | — |
| `MainThread.isCurrent()` | `static boolean isCurrent()` | Is the caller the main thread? | — |

## Usage example

```java
WorkerPool pool = new WorkerPool("multicore-worker", config.workerThreads());
pool.start();
pool.submit(() -> doCpuHeavyWork());
pool.shutdown();
```

## Dependencies

- **Allowed:** JDK concurrency, SLF4J.
- **Forbidden:** Minecraft world classes, the config module (the caller passes the size in).

## Notes and limitations

- Threads are **daemon** and named `multicore-worker-N`; the common pool is never used.
- A pool of size `0` never starts, which is how `workerThreads = 0` reproduces vanilla behaviour.
- `MainThread` is captured at `SERVER_STARTING`; it is not based on the thread name.

## Tests

`./gradlew test --tests "com.strefiz.multicore.thread.*"` — tests on the public API in
`src/test/java/com/strefiz/multicore/thread/`.
