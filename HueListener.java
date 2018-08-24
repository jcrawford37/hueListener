/*
Using VOCE which wraps sphinx4.
Using speech recognizer to try and turn hue lights on and off

Notes:  My Hue Bridge Json ID Numbers of lights.
		1 = bedroom
		2 = dungeon
		3 = living room (sala)
*/

import java.net.*; // URL and HttpConnection
import java.io.*;  //OutputStream Writer

class HueCommandParser {
	public static String getRoom(String inputStr) {
		if (-1 != inputStr.indexOf("house")) {
			return "house";
		} 
		else if (-1 != inputStr.indexOf("sala")) {
			return "3";
		}
		else if (-1 != inputStr.indexOf("bedroom")) {
			return "1"; //andrea is asleep don't let this work
		}
		else if (-1 != inputStr.indexOf("dungeon")) {
			return "2";
		}
		
		return "unknown";
	}

	public static String getOnOff(String inputStr) {
		if ( -1 != inputStr.indexOf(" on") ) { // LOL. added a space because dungeon has "on"
			return "on";
		}
		else if ( -1 != inputStr.indexOf("off")) {
			return "off";
		}

		return "unknown";
	}

}

class HueWebInterface {
	private static String bridgeUrl = "http://hueIP/api/";
	private static String bridgeUID = "<hue bridge id>/lights/";
	private static String state = "/state";

	private static String jsonBodyTrue = "{\"on\": true}";
	private static String jsonBodyFalse = "{\"on\": false}";

	public static void executeHueCommand(String room, String onOff) {
		String fullURL = bridgeUrl + bridgeUID + room + state;
		// System.out.println(" DEBUG Full URL: " + fullURL);
		System.out.println("Making API Call to HUE BRIDGE! \n");
		try {
			URL url = new URL(fullURL);
			HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
			httpCon.setDoOutput(true);
			httpCon.setRequestMethod("PUT");
			OutputStreamWriter out = new OutputStreamWriter(
		    httpCon.getOutputStream());
		    
		    // I think put json true or false here
			if (onOff.equals("on")) {
				out.write(jsonBodyTrue);
			} else {
				out.write(jsonBodyFalse);
			}
			out.close();
			httpCon.getInputStream();
		} catch (MalformedURLException e) {
			System.out.println("URL problem");
		} catch (IOException e){
			System.out.println("Some IOException" + e);
		}
	}

}

public class HueListener {
	
	public static void main(String[] args) {
		HueCommandParser parser = new HueCommandParser();
		HueWebInterface web = new HueWebInterface();
		boolean shouldSynth = false;
		boolean shouldRecognize = true;

		voce.SpeechInterface.init("/lib/voce", shouldSynth, shouldRecognize, 
			"./grammarFiles", "hue");

		System.out.println("\nHouseBot listening! What is my purpose?");
		System.out.println("You better not fucking say 'pass butter'!\n");

		boolean loop = true;
		while (loop) {
			boolean houseBotActivated = false;
			
			// Give it time to listen
			try {
				Thread.sleep(400);
			}

			catch(InterruptedException e) {
				System.out.println("Caught me some InterruptedException " + e);
			}
			
			while(!houseBotActivated && voce.SpeechInterface.getRecognizerQueueSize() > 0) {
				String voiceInput = voce.SpeechInterface.popRecognizedString();
				System.out.println("housebot debug loop heard : " + voiceInput);
				if ( -1 != voiceInput.indexOf("lazy ass hole")) {
					System.out.println("Yeah!? you wanted something?\n");
					houseBotActivated = true;
				}
			}


			int ghettoTimer = 0;
			int gTimerLimit = 20;

			while (houseBotActivated && (ghettoTimer < gTimerLimit)) {
					System.out.println("####################\n"
					+ "LISTENER ACTIVATED!\n"
					+ "####################");

				try {
					//without this sleep, the recognizer queue doesn't
					//seem to populate. It will populate during sleeps
					Thread.sleep(300);
				}
				catch(InterruptedException e) {
					System.out.println("Caught me some InterruptedException " + e);
				}

				while (voce.SpeechInterface.getRecognizerQueueSize() > 0) {
					String voiceInput =  voce.SpeechInterface.popRecognizedString();
					System.out.println("I think you said : " + voiceInput);
					System.out.println("Room: " + parser.getRoom(voiceInput));
					System.out.println("On/Off : " + parser.getOnOff(voiceInput));
					web.executeHueCommand(parser.getRoom(voiceInput), parser.getOnOff(voiceInput));

				}
				ghettoTimer++;
			}
			houseBotActivated = false;
			ghettoTimer = 0;
		}

		voce.SpeechInterface.destroy();
		System.exit(0);
	}
}

