package edu.sjsu.cmpe275.aop.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import edu.sjsu.cmpe275.aop.TweetStatsImpl;
import java.util.HashSet;

@Aspect
@Order(0)
public class StatsAspect {
	@Autowired TweetStatsImpl stats;
	@Autowired RetryAspect retryAspect;

	/*
	* Tweet details added in collection after successful execution of method
	* */
	@AfterReturning("execution(public void edu.sjsu.cmpe275.aop.TweetService.tweet(..))")
	public void tweetAfterAdvice(JoinPoint joinPoint) {
		try {
			String methodName = joinPoint.getSignature().getName();
			System.out.printf("After the executuion of the method %s\n", methodName);
			if(retryAspect.aspectResult) {
				Object obj[] = joinPoint.getArgs();
				String user = obj[0].toString();
				String tweetMessage = obj[1].toString();

				if (stats.longestTweet < tweetMessage.length()) {
					stats.longestTweet = tweetMessage.length();
				}
				if (tweetMessage.length() <= 140) {
					if (stats.userTweetsProductiveData.containsKey(user)) {
						stats.userTweetsProductiveData.put(user, stats.userTweetsProductiveData.get(user) + tweetMessage.length());
					} else {
						stats.userTweetsProductiveData.put(user, tweetMessage.length());
					}
					if (stats.userTweetsData.containsKey(user)) {
						stats.userTweetsData.put(user, stats.userTweetsData.get(user) + 1);
					} else {
						stats.userTweetsData.put(user, 1);
					}
				}
			}
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}


	/*
	* Follow details of users added in collection after successful execution of method
	* */
	@AfterReturning("execution(public void edu.sjsu.cmpe275.aop.TweetService.follow(..))")
	public void followAfterAdvice(JoinPoint joinPoint) {
		try{
			System.out.printf("After the executuion of the method %s\n", joinPoint.getSignature().getName());
			if(retryAspect.aspectResult){
				Object obj[] = joinPoint.getArgs();
				String follower = obj[0].toString();
				String followee = obj[1].toString();
				if(stats.userFollowers.containsKey(followee)){
					if(!stats.userFollowers.get(followee).contains(follower)){
						stats.userFollowers.get(followee).add(follower);
					}
					else {
						System.out.printf("User %s already follows user %s \n", follower, followee);
					}
				}
				else {
					HashSet<String> hs = new HashSet<String>();
					hs.add(follower);
					stats.userFollowers.put(followee, hs);
				}
			}
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}

	/*
	* Block details of users added in collection after successful execution of method
	* */
	@AfterReturning("execution(public void edu.sjsu.cmpe275.aop.TweetService.block(..))")
	public void dummyBlockAfterAdvice(JoinPoint joinPoint) {
		try{
			System.out.printf("After the executuion of the method %s\n", joinPoint.getSignature().getName());
			Object obj[] = joinPoint.getArgs();
			if(retryAspect.aspectResult) {
				String user = obj[0].toString();
				String blockedUser = obj[1].toString();
				if (stats.blockedUsers.containsKey(blockedUser)) {
					if (!stats.blockedUsers.get(blockedUser).contains(user)) {
						stats.blockedUsers.get(blockedUser).add(user);
					} else {
						System.out.printf("User %s already blocked user %s \n", user, blockedUser);
					}
				} else {
					HashSet<String> hs = new HashSet<String>();
					hs.add(user);
					stats.blockedUsers.put(blockedUser, hs);
				}
			}
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}

}
