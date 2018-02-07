
/**
  * First, we need to implement the parallel and task function by using
  * Java concurrent and Scala DynamicVariable
  */

import java.util.concurrent._

import scala.util.DynamicVariable


val forkJoinPool = new ForkJoinPool


/**
  * schedule will take an task (function) and return an ForkJoinTask
  * of type T, which comes from java.util.concurrent.ForkJoinTask
  *
  * A ForkJoinTask is a lightweight form of Future.
  * 
  * Future is a placeholder object for data that will exist in future,
  * and that data is supplied concurrently and can subsequently be used.
  *
  * So Future is like a single-use Observable object in Reactive Programming,
  * and it has operation isDone(), or wait for it to finish using get(), 
  * where the get() here is similar as the join() of ForkJoinTask objects.  
  *
  * Will explain more of ForkJoinTask with ForkJoinPool below.
  */

abstract class TaskScheduler {
  def schedule[T](body: => T): ForkJoinTask[T]
  def parallel[A, B](taskA: => A, taskB: => B): (A, B)
}

class DefaultTaskScheduler extends TaskScheduler {

  def schedule[T](body: => T): ForkJoinTask[T] = {
    /**
      * A RecursiveTask is a ForkJoinTask that returns a result.
      * 
      * It may split its work up into smaller tasks(ForkJoinTask), 
      * and merge the result of these smaller tasks into a
      * collective result. So we were able to split task
      * t by more smaller sub-tasks.
      */
    val t = new RecursiveTask[T] {
      def compute = body
    }

    /**
      * The Thread comes from scala concurrency that is
      * is built on top of the Java concurrency model.
      *
      * On Sun JVMs, with a IO-heavy workload, we can run
      * tens of thousands of threads on a single machine.
      *
      * The current thread could be a 
      * 1) ForkJoinPool used thread -> ForkJoinWorkerThread
      * 2) other non-related thread
      *
      * From Java document, ForkJoinWorkerThread is "A thread
      * managed by a ForkJoinPool, which executes ForkJoinTasks."
      *
      * A ForkJoinTask is a thread-like entity that is much lighter
      * weight than a normal thread. Huge numbers of tasks and sub-tasks
      * may be hosted by a small number of actual threads in a ForkJoinPool,
      * at the price of some usage limitations.
      *
      * It is because each actual threads in a ForkJoinPool hold itself
      * a dequeue where all its tasks are placed. So a actually thread
      * is considered a non-fork/join clients	or a fork/join computations
      *
      * If the current thread is ForkJoinWorkerThread, which means the
      * current thread is call from within fork/join computations,
      * RecursiveTask object will call t.fork() to (async) execute.
      *
      * If the current thread is a non-fork/join clients, it will call
      * forkJoinPool.execute(t) to (async) execute the task t.
      *
      * Here, the t is an ForkJoinTasks
      *
      * ------------------------------------------------------
      *             ForkJoinPool               | Unused Thread
      *                                        |
      *   Dequeue      Dequeue      Dequeue    |
      * -----------  -----------  -----------  |
      * | task F  |  |         |  |         |  |
      * -----------  -----------  -----------  |
      * | task D  |  | task E  |  |         |  |
      * -----------  -----------  -----------  |
      * | task A  |  | task B  |  | task C  |  |
      * -----------  -----------  -----------  |
      *  Thread 1      Thread 2    Thread 3    |   Thread 4
      * ------------------------------------------------------
      * 
      * ^ Dequeus won't communicate each other, and each task is an ForkJoinTasks
      *
      * References:
      * 1) https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ForkJoinTask.html
      * 2) https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ForkJoinPool.html
      * 3) https://docs.oracle.com/javase/7/docs/api/java/util/Deque.html
      * 4) http://www.javacreed.com/java-fork-join-example/
      */
    Thread.currentThread match {
      case wt: ForkJoinWorkerThread => t.fork()
      case _ => forkJoinPool.execute(t)
    }
    t
  }

  /**
    * In function parallel, variable right is an ForkJoinTasks here.
    *
    * ForkJoinTasks.join() executes asynchronously. It is used in
    * divide and conquer algorithms that can be multi threaded.
    *
    * The join() is different of invoke() since calling invoke waits
    * for the invoked task to complete. So your method in now not asynchronous.
    *
    * For ForkJoinTasks t, t.fork().join() will equal to t.invoke(),
    * but since fork() starts a process asynchronously and invoke starts
    * a process synchronously, the difference between t.fork().join() and
    * t.invoke() will be t.invoke() working on the current Thread between
    * invoking fork and join.
    *
    * Reference:
    * 1) https://stackoverflow.com/questions/17876144/fork-join-related-join-vs-get-vs-invoke
    *
    * @param taskA
    * @param taskB
    * @tparam A
    * @tparam B
    * @return
    */
  def parallel[A, B](taskA: => A, taskB: => B): (A, B) = {
    val right = schedule(taskB)
    val left = taskA

    (left, right.join())
  }
}

/**
  * DynamicVariable is used to bind values dynamically for the scope
  * specified in parameter-less closure.
  *
  * When different threads have different context to work on, instead of
  * passing context from function to function, we store them in thread
  * local variables aka dynamic variables.
  *
  * In the following case, when function task is created, each thread will
  * hold an TaskScheduler object that has different body of type T
  */

val scheduler = new DynamicVariable[TaskScheduler](new DefaultTaskScheduler)

def task[T](body: => T): ForkJoinTask[T] = {
  scheduler.value.schedule(body)
}

def parallel[A, B](taskA: => A, taskB: => B): (A, B) = {
  scheduler.value.parallel(taskA, taskB)
}

def parallel[A, B, C, D](taskA: => A, taskB: => B, taskC: => C, taskD: => D): (A, B, C, D) = {
  val ta = task {
    taskA
  }
  val tb = task {
    taskB
  }
  val tc = task {
    taskC
  }
  val td = taskD
  (ta.join(), tb.join(), tc.join(), td)
}

