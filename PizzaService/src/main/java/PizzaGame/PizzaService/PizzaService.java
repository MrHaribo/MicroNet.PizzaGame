package PizzaGame.PizzaService;

import micronet.annotation.MessageListener;
import micronet.annotation.MessageService;
import micronet.network.Context;
import micronet.network.Request;
import micronet.network.Response;
import micronet.network.StatusCode;
import micronet.serialization.Serialization;

@MessageService(uri = "mn://pizza")
public class PizzaService {
	
	@MessageListener(uri = "/order")
	public Response orderHandler(Context context, Request request) {

		PizzaType type = Enum.valueOf(PizzaType.class, request.getData());
		
		Pizza pizza = PizzaFactory.bake(type);
		
		return new Response(StatusCode.OK, Serialization.serialize(pizza));
	}
}

