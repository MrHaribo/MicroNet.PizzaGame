package PizzaGame.PizzaService;

import java.util.Arrays;

public class PizzaFactory {

	public static Pizza bake(PizzaType type) {
		
		Pizza pizza = new Pizza();
		pizza.setType(type);
		
		switch (type) {
		case Hawai:
			pizza.setIngredients(Arrays.asList("Prosciutto", "Ananas", "Tomato Sauce", "Mozzarella"));
			pizza.setPrice(15.20f);
			break;
		case Margherita:
			pizza.setIngredients(Arrays.asList("Tomato Sauce", "Mozzarella"));
			pizza.setPrice(12.90f);
			break;
		case Salami:
			pizza.setIngredients(Arrays.asList("Salami", "Tomato Sauce", "Mozzarella"));
			pizza.setPrice(14.10f);
			break;
		default:
			return null;
		}
		
		return pizza;
	}

}
