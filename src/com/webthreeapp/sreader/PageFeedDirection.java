package com.webthreeapp.sreader;

import java.util.Arrays;

public class PageFeedDirection{

	private final String[] directionDictionary = {"left","right"};
	private String state;

	public PageFeedDirection(String pageFeedDirection) {
		super();

		if(Arrays.asList(directionDictionary).contains(pageFeedDirection)){
			this.state = pageFeedDirection;
		}else{
			throw new Error("set left or right"); //leftかrightしか受け付けない
		}

	}

	public String getState() {
		return state;
	}

	public void setState(String pageFeedDirection) {
		if(Arrays.asList(directionDictionary).contains(pageFeedDirection)){
			this.state = pageFeedDirection;
		}else{
			throw new Error("set left or right"); //leftかrightしか受け付けない
		}
	}

	public boolean equals(Object obj) {
        // オブジェクトがnullでないこと
        if (obj == null) {
            return false;
        }

        // 同値性を比較
        if(obj instanceof PageFeedDirection){
        	return this.state == ((PageFeedDirection)obj).getState();
        }else if(obj instanceof String){
        	return this.state == (String)obj;
        }else{
        	return false; //同じ型か文字列でない
        }

    }



}