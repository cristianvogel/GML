/*
Generative Movement Language is a context-free grammar text generator.


 press space to generate
 press 's' to save a .txt and a .png
 press 'r' to see a breakdown of non-repeating/POS tagged/Open Class tokens derived from the generated result


 (c) cristian vogel 2010-2019

 RiTa natural language library by Daniel C. Howe
 http://www.rednoise.org/rita/

 */
package com.neverEngineLabs.GML2019;


import com.google.gson.internal.bind.JsonTreeReader;
import com.sonoport.freesound.FreesoundClient;
import com.sonoport.freesound.FreesoundClientException;
import com.sonoport.freesound.query.JSONResponseQuery;
import com.sonoport.freesound.query.search.SortOrder;
import com.sonoport.freesound.query.search.TextSearch;
import com.sonoport.freesound.query.sound.SoundInstanceQuery;
import com.sonoport.freesound.response.Response;

import com.sonoport.freesound.response.Sound;

import com.google.gson.*;

import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PGraphics;
import processing.data.JSONArray;
import processing.data.JSONObject;
import processing.data.StringList;


import rita.RiTa;


import javafx.scene.media.AudioClip;

import java.util.*;




public class RunGML extends PApplet {
	//constructor with field assignments
	private GrammarGML grammar = new GrammarGML(this);
	private String[] lines = { "Press spacebar to Generate...\nPress 's' to save...\nPress 'i' for info...\nPress 'r' to reduce..." };
	private String[] linesAlt ;
	private String currentGrammarFile = "grammarFiles/FlowerSpiral.json";
	private String latestTitle = "Welcome to Generative Movement Language!";
	private String latestTimeStamp = "Current grammar: "+currentGrammarFile;
	private Boolean savedFlag = false;
	private int generationCounter = 0;

	// todo: OSC setup
   // public OscP5 oscP5 = new OscP5(this, 8000); // listener

	//font sizes
    private final int  H1=24, P=20, TINY=12, TOKEN=32;
    private Map<Integer,PFont> fonts = new HashMap<>();

	private PGraphics offscreenBuffer;
	private boolean displayingInfo = false;
	private boolean displayingReduced = false;

	/**
	 * FreeSound.org
	 */

	FreesoundClient freesoundClient;

	private String clientId = "7RllqFNPuvjj7U34vMu5";
	private String clientSecret = "QPGtFa2wB0bMIHxffUhvYJMlQU0XxhZYtT9so0jE";

	// Audio
	AudioClip freeSound;

	////////////////////////


	public void settings() {

		size(1000, displayHeight );
		pixelDensity(displayDensity());
	}

	public void setup() {

		fonts.put(TINY, createFont("data/fonts/Lato-Italic.ttf", TINY, false));
		fonts.put(H1, createFont("data/fonts/Lato-Bold.ttf", H1, true));
		fonts.put(P, createFont("data/fonts/Lato-Regular.ttf", P, true));
		fonts.put(TOKEN, createFont("data/fonts/RobotoSlab-Regular.ttf", TOKEN, true));

	    grammar.loadFrom(currentGrammarFile); // todo:  user or random selection of new grammars from disk
		setFont(P);
		textAlign(CENTER, CENTER);
		setTitleBar(latestTitle + grammar.getLatestTimeStamp());
		displayGeneratedTextLayout(latestTitle, lines, 28);

		offscreenBuffer = createGraphics(1000, displayHeight ); //todo: page turn implementation

		freesoundClient = new FreesoundClient(clientId, clientSecret);
		freeSoundTextSearch("cars");


	}

	public void draw() {
		/*
		usually called every frame by Processing
		but we only need to draw the text Score from an interaction
		 so graphics are done inside grammar expansion event driven method
		 like this

		displayGeneratedTextLayout(latestTitle, lines, 28);
		*/

	}


	public void freeSoundTextSearch(String token) {

		Set<String> fields = new HashSet<>(Arrays.asList("id", "url", "previews", "tags"));

		final TextSearch textSearch = new TextSearch().searchString(token).sortOrder(SortOrder.SCORE).includeFields(fields);

		Response response = null;
		try {
			response = freesoundClient.executeQuery(textSearch);
		} catch (FreesoundClientException e) {
			e.printStackTrace();
		}

		int httpStatusCode = response.getResponseStatus();

		int lookup = 2;


		JsonElement results = new Gson().toJsonTree(response.getResults());
		//this gets a String out
		//String results = new Gson().toJson(response.getResults());

		println("Http status code = " + httpStatusCode);
		println("results:" + results);

		JsonElement url = results.getAsJsonArray().get(lookup).getAsJsonObject().get("url");
		println("item url:" + url.toString());

		JsonElement previews = results.getAsJsonArray().get(lookup).getAsJsonObject().get("previews");
		JsonElement mp3Hq = previews.getAsJsonObject().get("preview-hq-mp3");

		AudioClip soundClip = new AudioClip(mp3Hq.toString());
		soundClip.play(1);

		/**
		 * this lot of code will retrieve a SoundResponse from a integer unique ID
		 * work in progress
		 * https://freesound.org/docs/api/resources_apiv2.html#sound-instance
		 *
		 *
		JsonElement soundIdentifier = results.getAsJsonArray().get(lookup).getAsJsonObject().get("id");
		SoundInstanceQuery soundInstanceQuery = new SoundInstanceQuery(soundIdentifier.getAsInt());

		Response<Sound> soundResponse = null;
		try {
			soundResponse = freesoundClient.executeQuery(soundInstanceQuery);
		} catch (FreesoundClientException e) {
			e.printStackTrace();
		}

		JsonElement soundResults = new Gson().toJsonTree(soundResponse.getResults());
		**/

		/*
		AudioClip soundClip = new AudioClip(soundResponse.toString());
		soundClip.play(1);
		*/
	}




	private void displayGeneratedTextLayout(String title, String[] body, int lineHeight) {

		drawDecorativeBackground( 15, body.length + generationCounter);

		setFont(H1);
		text(title, width/2, lineHeight);

		setFont(TINY);
		text((savedFlag ? ("saved " + latestTimeStamp) : latestTimeStamp), width / 2, lineHeight*2);

		setFont(P);
		for (int j = 0; j < body.length; j++) {
			text(body[j], width/2, (height/5) + j * lineHeight);
		}


	}

	private void displayGeneratedTextLayout(String title, String[] body, int lineHeight, int FONT) {

		drawDecorativeBackground( 15, body.length + generationCounter);

		setFont(H1);
		text(title, width/2, H1+2);

		setFont(TINY);
		text((savedFlag ? ("saved " + latestTimeStamp) : latestTimeStamp), width / 2, lineHeight*2);

		setFont(FONT);
		for (int j = 0; j < body.length; j++) {
			text(body[j], width/2, (height/5) + j * lineHeight);
		}


	}

	private void setFont(int tag) {
		textFont(fonts.get(tag));
		textSize(tag);
	}

	private void drawDecorativeBackground(int backgroundGrey, int numberOfLines) {

		background(backgroundGrey);

		//fill(250);
		noiseDetail(8, 0.8f);

		if (numberOfLines > 1) {
			for (int i = 1; i < numberOfLines * 10; i++) {

				stroke(i+30, 80);
				strokeWeight(random(i));
				line(-50, (noise(i * 0.5f) * (height * map(i, 1, numberOfLines, 0.2f, 1))) - 200,
						width + 50, (noise(i * 0.501f) * (height * map(i, 1, numberOfLines, 0.2f, 1))) - 200);
			}
		}
	}


    private void expandGrammar() {
	lines = grammar.generateTextAndSplitAtLineBreak();
	savedFlag = false;
	generationCounter++;

	if (lines.length > 0) {
		/* lines = grammar.shuffle(lines);
		//todo: allow shuffle from a grammar callback */
		latestTitle = grammar.generateRhymingTitleFromLinesOfText(lines);//todo: more random title gen
		setTitleBar(latestTitle);
		latestTimeStamp = grammar.latestTimeStamp;
	} else {
		setTitleBar("There was a problem generating the text...");
	}


	displayGeneratedTextLayout(latestTitle, lines, 28);
}


	// was too easy to loose the text by clicking out of app and back in, so removed for now
	public void mouseClicked() {
	//expandGrammar();
	//println((Object) getGeneratedTextAsLines());
	}

	/**
	 * space bar expands grammar
	 * S saves png and txt of result
	 * I gets info about grammar
	 * R gets closed class words
	 */
	public void keyPressed() {
		if (key == ' ' ) {
			expandGrammar();
		}
		if ( key == 's' || key == 'S') {

			//try to save to disk, post status in window title
			if (saveGeneratedTextAndScreenshot(grammar.currentExpansion) ) {

				setTitleBar("Saved successfully "+grammar.timeStampWithDate());
				savedFlag = true;
				displayGeneratedTextLayout(latestTitle, lines, 28);

			}
			else { setTitleBar("ERROR: NOT SAVED"); savedFlag = false;  }
		};

		if ( key == 'i' || key == 'I') {

			displayingInfo=!displayingInfo;
			String info = grammar.displayInfo();


			if (info!="") {linesAlt = split(info, grammar.lineBreaker); println(linesAlt);}

			if (!displayingInfo) {

				displayGeneratedTextLayout(latestTitle, lines, 28);
			}
			else {

				displayGeneratedTextLayout("Grammar File Info", linesAlt, 22);
			}
		}

		if (key=='r' || key == 'R') {
			displayingReduced = !displayingReduced;

			displayReduced();
		}
	}

    /** save output to disk as txt and png
     *
     * @param outputStrings
     * @return true if successful
     */
	private Boolean saveGeneratedTextAndScreenshot(String[] outputStrings) {


		StringList sList = new StringList ( outputStrings );
		String title = latestTitle;

		String savePath = calcSketchPath()+"/data/Saved/"+RiTa.chomp(title)+"/";
		createPath(savePath);
		String fn = savePath + RiTa.chomp(title);
		String fnReduced = fn+"_reduced";
		//String fn = (System.getProperty("user.home"))+"/documents/"+title+".txt";

		//header
		sList.insert(0, title+"\n\n");

		//footer
		sList.append("\n\n"); //make some space
		sList.append("Generated:" + grammar.timeStampWithDate());
		println(fn);
		try {
			saveStrings(fn+".txt",sList.array());
			saveFrame(fn + ".png"); // save screen shot
			saveStrings(fnReduced+".txt", grammar.arrangeTokensIntoLines(grammar.currentExpansionReduced, 4));

			displayingReduced = true;
			displayReduced();
			saveFrame(fnReduced + ".png"); // save reduced words screen shot
			displayingReduced = false;

		} catch (Exception e) {
			background(255,0,0);
			return false;
		}

		return true;
	}


	public void setTitleBar(String s) {
	    surface.setTitle(s);
	}

	public void displayReduced() {
		if (displayingReduced) {


			displayGeneratedTextLayout(
					latestTitle + " (Reduced)",
					grammar.arrangeTokensIntoLines(grammar.currentExpansionReduced, 6),
					TOKEN+4,
					TOKEN
			);
		} else {
			displayGeneratedTextLayout(latestTitle, lines, TOKEN+4);
		}


	}




/// Java main

	public static void main(String[] args) {


		System.out.println("Running " + RunGML.class.getName());
		String[] options = {  RunGML.class.getName() };
		PApplet.main(options);
	}

}