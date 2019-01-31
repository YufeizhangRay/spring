package cn.zyf.spring.demo.aspect;

public class LogAspect {

	// 在调用一个方法之前，执行before方法
	public void before() {
		System.out.println("Invoker Before Method!!!");
	}

	// 在调用一个方法之后，执行after方法
	public void after() {
		System.out.println("Invoker After Method!!!");
	}
}
