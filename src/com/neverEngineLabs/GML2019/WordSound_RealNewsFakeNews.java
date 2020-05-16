/*
Generative Movement Language (GML) is a context-free grammar text generator.


 press space to generate
 press 's' to save a .txt and a .png
 press 'r' to see a breakdown of non-repeating/POS tagged/Open Class tokens derived from the generated result

	press 'p' to sonify the text using sounds from Freesound.org

 (c) cristian vogel 2010-2019

SonoPort Freesound-Java
https://github.com/Sonoport/freesound-java

RiTa natural language library by Daniel C. Howe
http://www.rednoise.org/rita/

 */
package com.neverEngineLabs.GML2019;


import com.google.gson.JsonElement;
import com.sonoport.freesound.FreesoundClient;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.data.StringList;
import rita.RiTa;
import rita.RiText;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.io.File;
import java.util.*;

import static com.neverEngineLabs.GML2019.FileIOHelpers.localAudioFile;
import static com.neverEngineLabs.GML2019.Type.*;


public class WordSound_RealNewsFakeNews extends PApplet implements IStreamNotify, ISearchNotify {
	//constructor with field assignments
	private GrammarGML grammar = new GrammarGML(this);
	private String[] lines = {
	        "Press 'G' to select text generator rules...\n\n" +
            "Press SPACEBAR to generate text...\n" + "or..\n"+
            "Press 'P' to listen to sonify the text... \n" +
			"Sounds will be retrieved from the FreeSound database."};
	private String[] linesAlt ;
	private String [] grammarFiles;
	private String currentGrammarFile = "data/grammarFiles/VerifiableNews.json";
	private String latestTitle = "Welcome to WordSound";
	private String latestTimeStamp = "Current grammar: "+currentGrammarFile;
	private Boolean savedFlag = false;

	private int generationCounter = 1;

	private Type _fonts = new Type() ;
	//public  int  H1=36, P=20, TINY=12, TOKEN=24;

	public RiText [] _picked;

	public String [] buttonLabels;
	private boolean displayingInfo = false;
	private boolean displayingReduced = false;

	File clickSound = new File ("data/sounds/click.wav"),
		sonifyGoSound = new File ("data/sounds/sonifyGo.wav"),
		noGoSound = new File ("data/sounds/noGo.wav");

	private RiText [] buttons;

	public String consoleStatus = "";

	/**
	 * FreeSound.org
	 */

	FreesoundClient freesoundClient;
	private String clientId = "7RllqFNPuvjj7U34vMu5";
	private String clientSecret = "QPGtFa2wB0bMIHxffUhvYJMlQU0XxhZYtT9so0jE";
	private ArrayList<Integer>  _taggedHits;

	//timers
	private float previousDuration = 0.1f;
	private String[] wordsToSonify;

	//other stuff
	private int clickCount = 0;
	private float _startTime;
	private Timer _localStatusTimer;
	public PGraphics offscreenBuffer;
	public PImage layout;
	public PImage consoleDisplay;

	//thread related
	ConcurrentLinkedDeque<JsonElement> searchResults = new ConcurrentLinkedDeque<>();
	Thread searchThreads[] = new Thread[100];
	private boolean streaming;

	////////////////////////


	public void settings() {

		size(1000, (int) (displayHeight * 0.75) );
		pixelDensity(displayDensity());

	}

	public void setup() {


	    RiTa.start(this);
		_fonts.init(this.g);
		consoleDisplay = new PImage(0,0);


	    grammar.loadFrom(currentGrammarFile);

		_fonts.setP();
		textAlign(CENTER, CENTER);
		rectMode(CORNERS);

		displayGeneratedTextLayout(latestTitle, lines, 28);

		offscreenBuffer = createGraphics(width, height ); //todo: page turn implementation

		setTitleBar("Loading, please wait...");
		freesoundClient = new FreesoundClient(clientId, clientSecret);
		_taggedHits = new ArrayList<>();

		// todo: set up some null safety checks - this is all quick and dirty
		grammarFiles = grammar.filesInSameDirectory(new File(currentGrammarFile));

		buttonLabels = new String[grammarFiles.length];
		for (int i = 0; i < grammarFiles.length; i++) {
			String fn = grammarFiles[i];
			buttonLabels [i] = grammar.toTitleCase( fn.toLowerCase().substring(0, fn.indexOf(".json") ));
		}
				try {
			String [] greet = {"welcome", "hello", "greeting", "hola", "hi", "greet", "welcoming", "hallo", "salutation", "hej"};
			//freeSoundTextSearchAndPlay(greet[RiTa.random(greet.length)], 5);
					setTitleBar(latestTitle + grammar.getLatestTimeStamp());
					setConsoleStatus(latestTitle + grammar.getLatestTimeStamp());
		} catch ( Exception e) {
					new AudioStreamer  (this, localAudioFile(clickSound), 1f);
					new AudioStreamer  (this, localAudioFile(clickSound), 1.5f, 0.9f);
					new AudioStreamer  (this, localAudioFile(clickSound), 2.0f, 0.5f);


			e.printStackTrace();
			setTitleBar( ">> PLEASE CHECK YOUR INTERNET CONNECTION <<");
			fill(200,10,0);
			String warning = ">> PLEASE CHECK YOUR INTERNET CONNECTION <<";
			text(warning, width - (textWidth(warning)), height-60);
		}



		//initialise grammar select UI
		RiText.defaultFont(_fonts.getFont(P));
		buttons =  RiText.createLines(this,grammarFiles, width/2f,height * 0.75f);
		for (int i = 0; i < buttons.length; i++) {
			buttons[i].boundingStroke(120).showBounds(true).align(CENTER);
			buttons[i].boundingFill(30);
			buttons[i].text(buttonLabels [i]);
		}

	}

	public void draw() {

		//todo: improve UI Classes and Methods

		drawDecorativeBackground( 15, 10* ((searchResults.isEmpty()) ? 10 : searchResults.size() + 10));
		image(layout,0,0);
		console();

		//audio streaming
		if (streaming)
		{
			sonifyGeneratedText();
		}

		if (generationCounter<1) {

			RiText.drawAll(buttons);
			for (RiText rt : buttons) rt.colorTo(0, 180, 120f, 255, 2);

			_picked = RiText.picked(mouseX, mouseY);
			if (_picked.length == 0) clickCount=0;
			if (_picked != null && _picked.length > 0) {

				RiText rt = _picked[_picked.length - 1];
				cursor(HAND);
				for (int i = 0; i < buttons.length; i++) {

					buttons[i].boundingFill(30);
					if (rt == buttons[i]) {
						buttons[i].boundingFill(60);


						if (mousePressed && clickCount==0) {
							clickCount++;
							currentGrammarFile = grammarFiles[i];

							new AudioStreamer(  this, localAudioFile(clickSound),0.1f, 1.1f).play();

							grammar.loadFrom("data/grammarFiles/"+currentGrammarFile);

							setConsoleStatus("Chosen new grammar file, expanding "+currentGrammarFile);
							expandGrammar();
						}
					}
				}

			} else {
				cursor(ARROW); generationCounter=0;
			}

		}
	}



	/**
     * text search of Freesound.org through the API which then sets up the playlist and player start
     * plus a bunch of overloads
	 * TODO: Encapsulate FreeSound processing
     *  @param searchString the word to search for
     * @param offset a delay to the start of audio playback in s
     * @param priority polyphony of 15 there can be a priority to voice offloading
     */

	public void freeSoundTextSearchAndPlay(String searchString, float offset, float maxDuration, int priority, int id) {

		// run search threads

			if ( searchThreads[id] == null) {
				AddTask task = new AddTask(this, searchResults, searchString, offset, maxDuration, priority, id);
				searchThreads[id] = new Thread(task);
				setConsoleStatus("Searching for sound " + searchString);
				searchThreads[id].start();
			}


		if ( searchResults.peekFirst() != null) {
			JsonElement results = searchResults.pollFirst();
			int _bounds = results.getAsJsonArray().size();
			if (_bounds == 0) {println("Empty result, skipping.."); return; }

			/**
			 * Search for Tag hits and try to pick from all tagged hits
			 * If no Tag matches, then the result probably
			 * has the Token somewhere else in the description
			 * so pick randomly from the results
			 */
			ArrayList<Integer>m_taggedHits = new ArrayList<>();
			m_taggedHits.clear();
			int selectedResult = 0;

			for (int i = 0; i < _bounds; i++)
			{
				JsonElement tags = results.getAsJsonArray().get(i).getAsJsonObject().get("tags");
				if (tags.toString().contains(searchString)) {
					m_taggedHits.add(i);
				}
			}

			// tags are not always abundant so try to pick from a set of at least 4 hits
			if (m_taggedHits.size() > 4) {
				for (int i = 0; i < 3 ; i++) {
					// do random pick three times
					selectedResult = m_taggedHits.get(RiTa.random(0, m_taggedHits.size()));
				}

				println ("Selecting from"+ m_taggedHits.size()+" tags");
			} else {
				for (int i = 0; i < 10 ; i++) {
					// do random pick a few times
					selectedResult = RiTa.random(0, _bounds);
				}
				println ("Not enough tags, selecting at random...");
			}

			JsonElement duration = results.getAsJsonArray().get(selectedResult).getAsJsonObject().get("duration");
			JsonElement url = results.getAsJsonArray().get(selectedResult).getAsJsonObject().get("url");
			JsonElement previews = results.getAsJsonArray().get(selectedResult).getAsJsonObject().get("previews");
			JsonElement mp3Hq = previews.getAsJsonObject().get("preview-hq-mp3");

			String _url = removeFirstAndLast(mp3Hq.toString()); //the GSON generated JSON primitives seem to come in with magic-quotes

			println ("Result " + selectedResult + " out of "+ results.getAsJsonArray().size() + " results for "+searchString+" with file duration: "+ duration.toString() + " duration:" + duration.getAsFloat()) ;

			if (! _url.isEmpty()) {
				searchResults.pollFirst();
				new AudioStreamer(this, _url, offset, priority, searchString, id);
			}
		}
	}

/*
	private void freeSoundTextSearchAndPlay(String token, float maxDuration) {
		try {
			//freeSoundTextSearchThenPlay(token, 0.1f, maxDuration, 1, millis());
			searchThreads(token, 0.1f, maxDuration, 1, millis());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}



    private void freeSoundTextSearchAndPlay(String token, float maxDuration, int voicePriority) {

			freeSoundTextSearchAndPlay(token, 0.1f, maxDuration, voicePriority,  millis());
	}

*/
	private void displayGeneratedTextLayout(String title, String[] body, int lineHeight) {

		offscreenBuffer = createGraphics(width, height);
		offscreenBuffer.rectMode(CORNERS);
		offscreenBuffer.beginDraw();

			offscreenBuffer.textFont(_fonts.getFont(H1));
			offscreenBuffer.fill(250);
			offscreenBuffer.text(title, width/3, lineHeight);


			offscreenBuffer.textFont(_fonts.getFont(TINY));
			offscreenBuffer.text((savedFlag ? ("saved " + latestTimeStamp) : latestTimeStamp), width / 2, lineHeight*2);

			offscreenBuffer.textFont(_fonts.getFont(P));

			if (body.length < 6) offscreenBuffer.textFont(_fonts.getFont(P));

			for (int j = 0; j < body.length; j++) {
				offscreenBuffer.text(body[j], width/6, (height/4) + j * lineHeight, width - (width/4), height-lineHeight);
			}
		offscreenBuffer.endDraw();
		layout = offscreenBuffer.get();
	}

	private void displayGeneratedTextLayout(String title, String[] body, int lineHeight, int FONT) {

		offscreenBuffer = createGraphics(width, height);
		offscreenBuffer.rectMode(CORNERS);
		offscreenBuffer.beginDraw();
			offscreenBuffer.fill(250);
			offscreenBuffer.textFont(_fonts.getFont(H1));

			offscreenBuffer.text(title, width/3, H1+2);

			offscreenBuffer.textFont(_fonts.getFont(TINY));

			offscreenBuffer.text((savedFlag ? ("saved " + latestTimeStamp) : latestTimeStamp), width / 2, lineHeight*2);
			offscreenBuffer.textFont(_fonts.getFont(P));

			if (body.length < 6) offscreenBuffer.textFont(_fonts.getFont(P));

			for (int j = 0; j < body.length; j++) {
				offscreenBuffer.text(body[j], width/6, (height/5) + j * lineHeight, width - (width/4), height-lineHeight);
			}
		offscreenBuffer.endDraw();
		layout = offscreenBuffer.get();
	}

    private void sonifyGeneratedText () {

		wordsToSonify = grammar.currentExpansionReduced;

		if (wordsToSonify==null) { println("No reduced words to sonify"); return;}

		streaming = true;

		for (int i = 0; i < wordsToSonify.length; i++)
		{
			if (i > 0)
				{ freeSoundTextSearchAndPlay(wordsToSonify[i], i * 3, 10, i % 15, i); }
			else
				// make first select potentially play for longer duration and start straightaway
				{ freeSoundTextSearchAndPlay(wordsToSonify[i], 0.5f, 25, 1, i); }
		}

    }


	private void drawDecorativeBackground(int backgroundGrey, int numberOfLines) {


			background(backgroundGrey,10);
			noiseDetail(8, 0.8f);
			float drift = frameCount * (0.0001f * generationCounter);
			if (numberOfLines > 1) {
				for (int i = 1; i < numberOfLines * 10; i++) {

					stroke(1f, i+(60 * (noise(i * 0.1f))), i+5f, 80f);
					strokeWeight(noise(i, frameCount*0.001f));
					line(-50, (noise(i * 0.5f) * (height * map(i, 1, numberOfLines, 0.2f + drift, 1))) - 200,
							width + 50, (noise(i * 0.501f) * (height * map(i, 1, numberOfLines, 0.2f - drift, 1))) - 200);
					strokeWeight(noise(i, drift));
					line(-50, (drift + noise(i * 0.45f) * (height * map(i, 1, numberOfLines, 0.2f, 1))) - 200,
							width + 50, (noise(i * 0.551f) * (height * map(i, 1, numberOfLines, 0.2f, 1))) - 200);
				}
			if (streaming) {
				pushMatrix();
				rotate(drift * 10);
				popMatrix();
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


	displayGeneratedTextLayout(latestTitle, lines, 34, _fonts.TOKEN);
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
			//new AudioStreamer  (this, localAudioFile(sonifyGoSound), 0.1f, 1f).start();

		}

		if (key == 'g' || key =='G') {
			generationCounter = 0;
			clickCount = 0;
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

		if (key=='p' || key=='P') {
			streaming = !streaming;
			sonifyGeneratedText();
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

	String removeFirstAndLast(String s) {
		String s1 = "";
		if (s.length() >2) {
			s1 = s.substring(1, s.length() - 1);
		}
		return s1;
	}

	public void setTitleBar(String s) {
	    surface.setTitle(s);
	}

	public void displayReduced() {
		if (displayingReduced) {

			displayGeneratedTextLayout(
					latestTitle + " (Reduced)",
					grammar.arrangeTokensIntoLines(grammar.currentExpansionReduced, 6),
					_fonts.TOKEN+4,
					_fonts.TOKEN
			);
		} else {
			displayGeneratedTextLayout(latestTitle, lines, _fonts.TOKEN+4);
		}
	}


	public void playbackStart(String url, String token) {
		if (!token.isEmpty()) {
			setTitleBar("Playing sound for \"" + token + "\"");
		}
	}

	public void console() {
		_fonts.setTINY();
		fill(128);

		text(consoleStatus, width/6, height-150, width - (width / 6), height - 25 );
	}

	@Override
	public void setConsoleStatus(String msg) {
		consoleStatus = msg;
	}

	public void playbackStatus(String threadStatus) {
		if (wordsToSonify != null) {

		if (((wordsToSonify[wordsToSonify.length-1] + " stopped").equals(threadStatus)) && searchResults.peekFirst() == null ) {

			streaming = false;
			setTitleBar(latestTitle+" was sonified!");
			setConsoleStatus(latestTitle+" was sonified!");
			}
		}
	}

	@Override
	public void taskComplete(Thread t, int id) {
		println("thread "+t+" with id " +id+ " ...task complete");
		//searchThreads[id] = null;
		try
		{
			t.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
/// Java main

	public static void main(String[] args) {

		System.out.println("Running " + WordSound_RealNewsFakeNews.class.getName());
		String[] options = {  WordSound_RealNewsFakeNews.class.getName() };
		PApplet.main(options);
	}
}