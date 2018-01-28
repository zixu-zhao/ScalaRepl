
/**
  * This definition gives a thread block of Account one
  * first, and then gives another thread block on both Account
  * one and Account two.
  * @param amount
  */

class Account (private var amount: Int = 0) {
  def transfer(target: Account, n: Int): Unit =
    this.synchronized {
      target.synchronized {
        this.amount -= n
        target.amount += n
      }
    }

  def getAmount: Int = this.amount
}

def startThread (a: Account, b: Account, n: Int) = {
  val t = new Thread {
    override def run(): Unit = {
      for (_ <- 0 until 10) {
        a.transfer(b, 1)
      }
    }
  }

  t.start()
  t
}

val accountOne = new Account(1000)
val accountTwo = new Account(500)

val tOne = startThread(accountOne, accountTwo, 100)
val tTwo = startThread(accountTwo, accountOne, 200)

/**
  * This causes a deadlock because both tOne and tTwo are
  * waiting release from each other, and nobody will release
  * first, so there exists an deadlock and the program will
  * never ends
  */

tOne.join()
tTwo.join()

/**
  * Amount in either accountOne or accountTwo won't be changed
  * in this case
  */

print(accountOne.getAmount, accountTwo.getAmount)

/**
  * Create a singleton object that is used to simulate
  * unique thread uid from system
  */

object generateUID {
  private var initial: Int = 0
  def getUniqueUid(): Int = {
    initial = initial + 1
    initial
  }
}

/**
  * Implement another way of Account that compares thread uid to decide
  * the order of execution to solve the deadlock
  * @param amount
  */
class AccountSolveDeadlock (private var amount: Int = 0) {

  private val uid = generateUID.getUniqueUid()

  private def _lockAndTransfer(target: AccountSolveDeadlock, n: Int): Unit =
    this.synchronized {
      target.synchronized {
        this.amount -= n
        target.amount += n
      }
    }

  def transfer(target: AccountSolveDeadlock, n: Int): Unit =
    if (this.uid < target.uid) this._lockAndTransfer(target, n)
    else target.transfer(this, n)

  def getAmount: Int = this.amount
}

def startThreadSolveDeadlock (a: AccountSolveDeadlock, b: AccountSolveDeadlock, n: Int) = {
  val t = new Thread {
    override def run(): Unit = {
      for (_ <- 0 until 10) {
        a.transfer(b, 1)
      }
    }
  }

  t.start()
  t
}

val accountThree = new AccountSolveDeadlock(1000)
val accountFour = new AccountSolveDeadlock(2000)

val tThree = startThreadSolveDeadlock(accountThree, accountFour, 100)
val tFour = startThreadSolveDeadlock(accountFour, accountThree, 200)

tThree.join()
tFour.join()

/**
  * After solving the deadlock, the results of both accountThree
  * and accountFour are changed successfully.
  */

print(accountThree.getAmount, accountFour.getAmount)


/**
  * For JVM there are two rules
  * 1. Two threads writing to separate locations in memory do not
  * need synchronization
  *
  * 2. A thread X that calls to join the thread Y is guaranteed to
  * observe all the writes by thread Y after the join returns
  */
