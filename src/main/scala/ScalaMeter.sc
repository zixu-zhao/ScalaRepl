// ScalaMeter Library is used for check code performance
import org.scalameter._

def runningOne = measure {
  (0 until 1000000).toArray
}

/**
  * Following snippet gives very different performance result because
  * JVM undergoes a period of warmup, and after which it achieves its
  * maximum performance.
  * 1. the program is interpreted
  * 2. parts of the program are compiled into machine code
  *    JVM is smart so it can compile some code which will
  *    run frequently
  * 3. JVM may choose to apply additional dynamic optimizations
  *    on some codes that runs very very frequently to ensure
  *    they are as fast as possible
  * 4. eventually, the program reaches steady state
  */

for (i <- 1 to 3) print(s"${i}th running time: $runningOne \n")

/**
  * So ScalaMeter could helps to skip the warmer step and achieve
  * a steady state on performance
  * @return
  */
def steadyState = withWarmer(new Warmer.Default) measure {
  (0 until 1000000).toArray
}

for (i <- 1 to 3) print(s"${i}th running time: $steadyState \n")


/**
  * Also, configuration clause allows specifying various parameters
  * @return
  */

def customizedTimeMeasure = config(
  Key.exec.minWarmupRuns -> 5,
  Key.exec.maxWarmupRuns -> 10,
  Key.verbose -> true
) withWarmer new Warmer.Default measure {
  (0 until 1000000).toArray
}

for (i <- 1 to 3) print(s"${i}th running time: $customizedTimeMeasure \n")


/**
  * Also the ScalaMeasure could measure other things like memory allocation
  *
  * MemoryFootprint will measure the total amount of memory occupied by the object
  * @return
  */
def memoryMeasure = withMeasurer(new Measurer.MemoryFootprint) measure {
  (0 until 1000000).toArray
}

/**
  * If the measure didn't hit the steady state, it will gives meaningless
  * result like negative number. So we need to running a several times to
  * ensure it really hits the steady state.
  */
for (i <- 1 to 3) print(s"${i}th running time: $memoryMeasure \n")