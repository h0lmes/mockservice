package com.mockservice;

import org.springframework.boot.test.context.SpringBootTest;

import java.util.stream.Stream;

@SpringBootTest
class MockserviceApplicationTests {

	private static class TestObject {
		String email;

		public TestObject(String email) {
			this.email = email;
		}

		public String getEmail() {
			return email;
		}
	}

	// just a small test of Optional
	public static void main(String[] args) {
		String s = Stream.of(new TestObject("a")).filter(e -> "b".equals(e.getEmail())).findFirst()
				.map(TestObject::getEmail).orElse("c");
		System.out.println(s);
	}
}
