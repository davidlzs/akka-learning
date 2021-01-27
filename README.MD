* [Akka typed persistence side effects](https://github.com/akka/akka/issues/25619)
    - If side-effect happens before persist - you risk side-effect happened but no event persited
    - If side-effect happens after persist - you risk to event persisted, and side-effect not happen. however, you can confirm side effect then store another event that it has performed, with this approach you can achieve at-least-once if side effect are idempotent (ok to retry several times)
* [Akka Persistence EventSource side-effect](https://doc.akka.io/docs/akka/current/typed/persistence.html#effects-and-side-effects)
  - thenRun (newState -> subscriber.tesll(newState)
  - Any side effects are executed on an at-most-once basic and will not be executed if the persist fails.
  - `Side effects are not run when the actor is restarted or started again after being stopped. You may inspect the state when receiving the RecoveryCompleted signal and execute that have not been acknowledged at that point. That may result in executing side effect more than once.`
  - It's possible to execute a side effect before persisting the event, but that can result in that the side effect is performed, but the event is not stored if the persistence fails.
  
  
  
  
