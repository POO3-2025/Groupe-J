package be.helha.poo3.serverpoo;

import be.helha.poo3.serverpoo.models.Item;
import be.helha.poo3.serverpoo.utils.ConnexionMongoDB;
import be.helha.poo3.serverpoo.utils.DynamicClassGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class ServerPooApplication {

	public static void main(String[] args) {

		ConnexionMongoDB db = ConnexionMongoDB.getInstance();

		DynamicClassGenerator dynamicClassGenerator = DynamicClassGenerator.getInstance();

		dynamicClassGenerator.generate(db);
	}

}
