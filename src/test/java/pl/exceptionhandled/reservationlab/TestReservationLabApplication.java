package pl.exceptionhandled.reservationlab;

import org.springframework.boot.SpringApplication;

public class TestReservationLabApplication {

	public static void main(String[] args) {
		SpringApplication.from(ReservationLabApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
