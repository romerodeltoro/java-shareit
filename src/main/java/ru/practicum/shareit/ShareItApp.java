package ru.practicum.shareit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ShareItApp {

	public static void main(String[] args) {
		SpringApplication.run(ShareItApp.class, args);

		/*User user = new User("Fedia", "q@q.com");
		User user2 = new User("Vasia", "q.ru");
		ItemRequest request = new ItemRequest("desc", user, LocalDateTime.now());

		Item item = new Item("Makita", "Perforator", false, user, request);
		Item item2 = new Item("Makita", "Perforator", false, user2, request);
		System.out.println(item2.getId() + ", " + item.getId());
		System.out.println(user);
		System.out.println(user2);*/

		
	}

}
