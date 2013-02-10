import collection.mutable.Stack
import org.scalatest.FunSuite

class MyTest extends FunSuite {
  test("foo") {
    println("Hello World")
    val stack = new Stack[Int]
    stack.push(1)
    val result = stack.pop
    assert(stack.size === 0)
  }
}
