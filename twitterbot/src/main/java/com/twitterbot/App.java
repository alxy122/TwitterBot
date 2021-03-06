package com.twitterbot;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import io.github.redouane59.twitter.IAPIEventListener;
import io.github.redouane59.twitter.TwitterClient;
import io.github.redouane59.twitter.dto.tweet.MediaCategory;
import io.github.redouane59.twitter.dto.tweet.Tweet;
import io.github.redouane59.twitter.dto.tweet.entities.MediaEntity;
import io.github.redouane59.twitter.signature.TwitterCredentials;

public class App {
    public static void main(String[] args) throws JsonParseException, JsonMappingException, IOException {
        TwitterClient twitterClient = new TwitterClient(
                TwitterClient.OBJECT_MAPPER.readValue(new File("keys.json"), TwitterCredentials.class));

        // twitterClient.addFilteredStreamRule("#alxy122", "#alxy122");

        IAPIEventListener listener = new IAPIEventListener() {
            @Override
            public void onStreamError(int httpCode, String error) {
                System.out.println(error);
            }

            @Override
            public void onTweetStreamed(Tweet tweet) {
                if (tweet.getAuthorId() != "726546513514606593") {
                    List<? extends MediaEntity> mediaList = tweet.getMedia();
                    if (mediaList != null) {
                        try {
                            InputStream in = new URL(mediaList.get(0).getMediaUrl()).openStream();
                            String mediaID = twitterClient
                                    .uploadMedia("test", in.readAllBytes(), MediaCategory.TWEET_IMAGE).getMediaId();
                            twitterClient.postTweet("Copy of your picture, "+twitterClient.getUserFromUserId(tweet.getAuthorId()).getDisplayedName()+" :)", tweet.getId(), mediaID);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else{
                        twitterClient.postTweet("Thanks for using my hashtag, "+twitterClient.getUserFromUserId(tweet.getAuthorId()).getDisplayedName(), tweet.getId());
                    }
                }
            }

            @Override
            public void onUnknownDataStreamed(String json) {
                System.out.println(json);
            }

            @Override
            public void onStreamEnded(Exception e) {
            }
        };
        twitterClient.startFilteredStream(listener);
    }
}