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


import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.sonoport.freesound.FreesoundClient;
import com.sonoport.freesound.FreesoundClientException;
import com.sonoport.freesound.query.search.SearchFilter;
import com.sonoport.freesound.query.search.SortOrder;
import com.sonoport.freesound.query.search.TextSearch;
import com.sonoport.freesound.response.Response;
import processing.core.PApplet;
import processing.data.StringList;
import rita.RiTa;
import rita.RiText;


import java.io.File;
import java.util.*;

import static com.neverEngineLabs.GML2019.FileIOHelpers.localAudioFile;


public class GML_SonifiedHaikus extends PApplet implements IStreamNotify {
	//constructor with field assignments
	private GrammarGML grammar = new GrammarGML(this);
	private String[] lines = {
	        "Press 'G' to choose a grammar file...\n\n" +
            "then press SPACEBAR to generate a new text...\n\n" +
            "or press 'S' to save the text to disk...\n\n" +
            "or press 'P' to play the text as sound." };
	private String[] linesAlt ;
	private String [] grammarFiles;
	private String currentGrammarFile = "data/grammarFiles/VerifiableNews.json";
	private String latestTitle = "Welcome to WordSound!";
	private String latestTimeStamp = "Current grammar: "+currentGrammarFile;
	private Boolean savedFlag = false;
	private int generationCounter = 1;

	private Type _fonts = new Type() ;
	public  int  H1=36, P=20, TINY=12, TOKEN=24;

	public RiText [] _picked;
	public String [] buttonLabels;
	private boolean displayingInfo = false;
	private boolean displayingReduced = false;

	File clickSound = new File ("data/sounds/click.wav"),
		sonifyGoSound = new File ("data/sounds/sonifyGo.wav"),
		noGoSound = new File ("data/sounds/noGo.wav");

	private RiText [] buttons;

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

	private int clickCount = 0;
	private float _startTime;
	private Timer _localStatusTimer;

	////////////////////////


	public void settings() {

		size(1000, (int) (displayHeight * 0.75) );
		pixelDensity(displayDensity());

	}

	public void setup() {

	    RiTa.start(this);
		_fonts.init(this.g);

	    grammar.loadFrom(currentGrammarFile);

		_fonts.setP();
		textAlign(CENTER, CENTER);
		rectMode(CORNERS);

		displayGeneratedTextLayout(latestTitle, lines, 28);

		//offscreenBuffer = createGraphics(1000, displayHeight ); //todo: page turn implementation

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
			String [] greet = {"welcome", "hello", "greeting", "hola", "hi", "greet", "welcoming"};
			freeSoundTextSearchThenPlay(greet[RiTa.random(greet.length)], 5);
					setTitleBar(latestTitle + grammar.getLatestTimeStamp());
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
		buttons =  RiText.createLines(this,grammarFiles, width/2f,height/2f);
		for (int i = 0; i < buttons.length; i++) {
			buttons[i].boundingStroke(120).showBounds(true).align(CENTER);
			buttons[i].boundingFill(30);
			buttons[i].text(buttonLabels [i]);
		}
	}


    /**
     * Processing3 update method called every frame
     */
	public void draw() {

		//todo: make UI Classes and Methods


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

							new AudioStreamer(  this, localAudioFile(clickSound),0.1f, 1.1f).start();
							grammar.loadFrom("data/grammarFiles/"+currentGrammarFile);

							println("Chosen new grammar file "+currentGrammarFile);


						}
					}
				}

			} else { cursor(ARROW); generationCounter=0;}

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

	public void freeSoundTextSearchThenPlay(String searchString, float offset, float maxDuration, int priority, int id) throws InterruptedException {


		Set<String> fields = new HashSet<>(Arrays.asList("id", "url", "previews", "tags", "duration"));
		SearchFilter _filter1 = new SearchFilter("ac_loudness", "[-23 TO 6]" );
		SearchFilter _filter2 = new SearchFilter("duration", "[1 TO "+maxDuration+"]" );


		println("Searching for \""+searchString+"\"");
		final TextSearch textSearch =
                new TextSearch()
                        .searchString(searchString)
                        .sortOrder(SortOrder.CREATED_DESCENDING)
                        .filter(_filter1).filter( _filter2).includeFields(fields)
                        .pageSize(RiTa.random(50,150));

		Response response = null;
		try {
			response = freesoundClient.executeQuery(textSearch);
		} catch (FreesoundClientException e) {
			e.printStackTrace();
		}

		int httpStatusCode = response.getResponseStatus();

		if (httpStatusCode == 429) {
			println("Http status code = " + httpStatusCode +": fail");
			setTitleBar("HTTP Error 429: too many requests - please try again later");
			return;
		}

		if (httpStatusCode == 400) {
			println("Http status code = " + httpStatusCode +": fail");
			setTitleBar("HTTP Error" + httpStatusCode);
			return;
		}


		JsonElement results = new Gson().toJsonTree(response.getResults());
		//this would get a String out
		//String results = new Gson().toJson(response.getResults());

		int _bounds = results.getAsJsonArray().size();
		if (_bounds == 0) {println("Empty result, skipping.."); return; }

		/**
		 * Search for Tag hits and try to pick from all tagged hits
		 * If no Tag matches, then the result probably
		 * has the Token somewhere else in the description
		 * so pick randomly from the results
		 */

		_taggedHits.clear();
		int selectedResult = 0;

		for (int i = 0; i < _bounds; i++)
		{
			JsonElement tags = results.getAsJsonArray().get(i).getAsJsonObject().get("tags");
			if (tags.toString().contains(searchString)) {
				_taggedHits.add(i);
			}
		}


		// tags are not always abundant so try to pick from a set of at least 4 hits
		if (_taggedHits.size() > 4) {
			for (int i = 0; i < 3 ; i++) {
				// do random pick three times
				selectedResult = _taggedHits.get(RiTa.random(0, _taggedHits.size()));
			}

			println("Selecting from"+_taggedHits.size()+" tags");
		} else {
			for (int i = 0; i < 3 ; i++) {
				// do random pick three times
				selectedResult = RiTa.random(0, _bounds);
			}
			println("Not enough tags, selecting at random...");
			}

		JsonElement duration = results.getAsJsonArray().get(selectedResult).getAsJsonObject().get("duration");
		JsonElement url = results.getAsJsonArray().get(selectedResult).getAsJsonObject().get("url");
		JsonElement previews = results.getAsJsonArray().get(selectedResult).getAsJsonObject().get("previews");
		JsonElement mp3Hq = previews.getAsJsonObject().get("preview-hq-mp3");

		String _url = removeFirstAndLast(mp3Hq.toString()); //the GSON generated JSON primitives seem to come in with magic-quotes



		float shortestDuration = min(offset, previousDuration);
        println ("Result " + selectedResult + " out of "+ results.getAsJsonArray().size() + " results for "+searchString+" with file duration: "+ duration.toString() + " start offset:" + shortestDuration) ;

		// background audio runnable
		AudioStreamer _audioStreamer = new AudioStreamer(this, _url, shortestDuration, priority,searchString, id);
		//println("Threads:" + Thread.activeCount());

        _audioStreamer.start();
		previousDuration = duration.getAsFloat();
	}



	private void freeSoundTextSearchThenPlay(String token, float maxDuration) {
		try {
			freeSoundTextSearchThenPlay(token, 0.1f, maxDuration, 1, millis());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

    private void freeSoundTextSearchThenPlay(String token, float maxDuration, int voicePriority) {
		try {
			freeSoundTextSearchThenPlay(token, 0.1f, maxDuration, voicePriority,  millis());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}



    /**
     * the following code will retrieve a SoundResponse from a integer unique ID
     * work in progress for future feature extraction purposes
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

    String removeFirstAndLast(String s) {
        String s1 = "";
        if (s.length() >2) {
            s1 = s.substring(1, s.length() - 1);
        }
        return s1;
    }


	private void displayGeneratedTextLayout(String title, String[] body, int lineHeight) {

		drawDecorativeBackground( 15, body.length + generationCounter);


		_fonts.setH1();
		text(title, width/2, lineHeight);


		_fonts.setTINY();
		text((savedFlag ? ("saved " + latestTimeStamp) : latestTimeStamp), width / 2, lineHeight*2);

		_fonts.setP();

		//if (currentGrammarFile.equals("haikuGrammar.json")) _fonts.setTOKEN();

        if (body.length < 6) _fonts.setTOKEN();

    for (int j = 0; j < body.length; j++) {
			text(body[j], width/6, (height/4) + j * lineHeight, width - (width/6), height-lineHeight);
		}
	}
	private void displayGeneratedTextLayout(String title, String[] body, int lineHeight, int FONT) {

		drawDecorativeBackground( 15, body.length + generationCounter);

		_fonts.setH1();
		text(title, width/2, _fonts.H1+2);

		_fonts.setTINY();
		text((savedFlag ? ("saved " + latestTimeStamp) : latestTimeStamp), width / 2, lineHeight*2);

		_fonts.setP();

		//if (currentGrammarFile.equals("haikuGrammar.json")) _fonts.setTOKEN();

        if (body.length < 6) _fonts.setTOKEN();

		for (int j = 0; j < body.length; j++) {
			text(body[j], width/6, (height/5) + j * lineHeight, width - (width/6), height-lineHeight);
		}


	}
    private void sonifyGeneratedText () throws InterruptedException {

		wordsToSonify = grammar.currentExpansionReduced;

         if (wordsToSonify==null) { println("No reduced words to sonify"); return;}
		setTitleBar("Loading audio...");
		//15 voices max?

		for (int i = 0; i < wordsToSonify.length; i++)
		{
			if (i > 0)
				{freeSoundTextSearchThenPlay(wordsToSonify[i], i * 3, 45, i % 15, i);}
			else // make first select potentially play for longer duration and start straightaway
				{freeSoundTextSearchThenPlay(wordsToSonify[i], 0.5f, 45, 1, i);}
		}

    }





	private void drawDecorativeBackground(int backgroundGrey, int numberOfLines) {

		background(backgroundGrey);
		noiseDetail(8, 0.8f);
		if (numberOfLines > 1) {
			for (int i = 1; i < numberOfLines * 10; i++) {

				stroke(1f, i+(60 * (noise(i * 0.1f))), i+5f, 80f);
				strokeWeight(random(i));
				line(-50, (noise(i * 0.5f) * (height * map(i, 1, numberOfLines, 0.2f, 1))) - 200,
						width + 50, (noise(i * 0.501f) * (height * map(i, 1, numberOfLines, 0.2f, 1))) - 200);
				strokeWeight(random(i));
				line(-50, (noise(i * 0.45f) * (height * map(i, 1, numberOfLines, 0.2f, 1))) - 200,
						width + 50, (noise(i * 0.551f) * (height * map(i, 1, numberOfLines, 0.2f, 1))) - 200);
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

			try {
				sonifyGeneratedText();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
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
					_fonts.TOKEN+4,
					_fonts.TOKEN
			);
		} else {
			displayGeneratedTextLayout(latestTitle, lines, _fonts.TOKEN+4);
		}
	}


/// Java main

	public static void main(String[] args) {


		System.out.println("Running " + GML_SonifiedHaikus.class.getName());
		String[] options = {  GML_SonifiedHaikus.class.getName() };
		PApplet.main(options);
	}


	public void playbackStart(String url, String token) {
		if (!token.isEmpty())
		setTitleBar("Sonifying \""+token+"\"");
	}


	public void playbackStatus(String threadStatus) {
		if (wordsToSonify != null) {
		if ((wordsToSonify[wordsToSonify.length-1] + " stopped").equals(threadStatus)) {
			setTitleBar(latestTitle+" was sonified!");
			}
		}
	}
}