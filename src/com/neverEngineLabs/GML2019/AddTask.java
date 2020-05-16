package com.neverEngineLabs.GML2019;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.sonoport.freesound.FreesoundClient;
import com.sonoport.freesound.FreesoundClientException;
import com.sonoport.freesound.query.search.SearchFilter;
import com.sonoport.freesound.query.search.SortOrder;
import com.sonoport.freesound.query.search.TextSearch;
import com.sonoport.freesound.response.Response;
import rita.RiTa;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;

import static processing.core.PApplet.min;
import static processing.core.PApplet.println;

public class AddTask implements Runnable {

        private ConcurrentLinkedDeque<JsonElement> m_searchResults;

        private FreesoundClient m_freesoundClient;
        private String clientId = "7RllqFNPuvjj7U34vMu5";
        private String clientSecret = "QPGtFa2wB0bMIHxffUhvYJMlQU0XxhZYtT9so0jE";
        private String m_searchString = "";
        private float m_offset, m_maxDuration;
        private int m_priority, m_id;

        ISearchNotify searchNotify;

        public AddTask(ISearchNotify main, ConcurrentLinkedDeque list, String searchString, float offset, float maxDuration, int priority, int id) {
        this.searchNotify = main;
        this.m_searchResults = list;

        this.m_freesoundClient = new FreesoundClient(clientId, clientSecret);
        this.m_offset = offset;
        this.m_searchString = searchString;
        this.m_maxDuration = maxDuration;
        this.m_priority = priority;
        this.m_id = id;
        }

@Override
public void run() {

                // store search results
        println( "Running: "+Thread.currentThread().getName());
        freeSoundTextSearch( searchNotify, m_searchString, m_offset, m_maxDuration, m_priority, m_id);

        }

        public void freeSoundTextSearch(ISearchNotify main, String searchString, float offset, float maxDuration, int priority, int id)  {

                searchNotify = main;
                Set<String> fields = new HashSet<>(Arrays.asList("id", "url", "previews", "tags", "duration"));
                SearchFilter _filter1 = new SearchFilter("ac_loudness", "[-23 TO 6]" );
                SearchFilter _filter2 = new SearchFilter("duration", "[1 TO "+maxDuration+"]" );


                println("Searching for \""+searchString+"\"");
               // setConsoleStatus("Searching for \""+searchString+"\"");
                final TextSearch textSearch =
                        new TextSearch()
                                .searchString(searchString)
                                .sortOrder(SortOrder.CREATED_DESCENDING)
                                .filter(_filter1).filter( _filter2).includeFields(fields)
                                .pageSize(RiTa.random(25,150));

                Response response = null;
                try {
                        response = m_freesoundClient.executeQuery(textSearch);
                } catch (FreesoundClientException e) {
                        e.printStackTrace();
                }

                int httpStatusCode = response.getResponseStatus();

                if (httpStatusCode == 429) {
                        println("Http status code = " + httpStatusCode +": fail");
                        //setTitleBar("HTTP Error 429: too many requests - please try again later");
                        return;
                }

                if (httpStatusCode == 400) {
                        println("Http status code = " + httpStatusCode +": fail");
                       // setTitleBar("HTTP Error" + httpStatusCode);
                        return;
                }

                // add the entire JSON payload to the ConcurrentLinkedDeque...

                JsonElement results = new Gson().toJsonTree(response.getResults());

                m_searchResults.add(results);
                searchNotify.taskComplete(Thread.currentThread(), m_id);

        }

}