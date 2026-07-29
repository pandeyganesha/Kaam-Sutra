# Roadmap to create this project

I don't have any SRS or strict image of how the project would look like. Hence, I am creating small tasks here and will make incremental changes.

Task 1:

On home page, let us first add an `+` button on bottom right side which creates a card with two fields i.e., Task name and Money.
These tasks will be daily tasks, that we need to do everyday. It will have the option to check or uncheck the task.

On top of the screen, there will be total screen which gets updated as we check/uncheck tasks.

## Sub steps to get above home page screen

- Add total money card on the top of the screen.
- Add button on bottom right side.
- On button press show a card with two fields with edit text
- On save, create a task with option to check or uncheck the task.
- Update the Net Worth when tasks/checked on unchecked.
- Persist the net worth in local storage
- After every new day, reset all the tasks to unchecked state and mark each task as not done and check/uncheck would again add/subtract the net worth.


Things I want to track:

- For recurring tasks ( daily/weekly/monthly) we earn points if done else gets deducted from our net worth.
- For streak, we give extra points.
- There can be two types of Goals, time bounded and unbounded. Or we can say we only have time bounded goals, and by setting "Indefinite" as time we can make a task unbounded, hence no penalty applies on it.
- But time bounded goals, if we do not achieve it, it's value starts decaying every week/day (have not thought which to choose yet) until it reaches a bare minimum and that point it is converted to unbounded goal and with still some value.

---
As some computer scientist said ( Forgot the name) that we must first design the Data Structure to support our problem and then only proceed ahead.
So the time has come when I need to think how to handle all this data.

Questions I am facing:
- Should Tasks and goals be two different entities or same? A task goal is task with no recurrence?
- Should time bounded and unbounded goals be two different things or as I have thought that unbounded goals are time bounded goals with indefinite time period?

Let us start with some example and try to handle the data for that.

1. Simple daily task. It needs to have a `name`, `pointsDelta`, `status`
2. Weekly task. It is same like daily task, just the recurrence is set to week.

Right now I am feeling to intuition to make everything a task and see how it goes. Hence by observing above two tasks I can add one more attribute to a task, that is recurrence.

3. Monthly task. It can again be handled with `recurrence`.
4. Goals. These are just like tasks, without `recurrence` but have time limit. Which means we we want to fit it in the definition of `task` we need to track if it is recurring in nature or deadline based.

After observing `Goals`, we have two path. One is to treat it differently or to treat it as `task` and hence we need to label it with `isRecurring` or `isDeadlineBased` option.
So either we simply have different class to represent each or to make them fall under same category and then handle the logic to make the data consistent, like can not choose `recurring` and `isDeadlineBased` options together.

And after analyzing both things, I feel like keeping goals as different entity seems better option. Why?
- Because first of all we won't have to handle the logic to avoid conflicting states.
- Second and then we can keep another categories to simply work on goals as they are different from tasks.
- In our life we treat both as different things. Tasks seems to be the procedure and Goals seems to be the result.
- We set tasks according to goal, hence each should be treated differently.

Conclusion. We have Goals as separate category.

Earlier I was not sure if I should create it as todo app or what, but now the image is getting clearer. I want to build habit tracker. It is just an application that I want to build for myself ( and can be used by others as well ) to help me in doing things that improves me as a person with good habits and metrics. No one is stopping me/you from adding a simple TODO task in it and get points or by claiming 1000 points for no reason.

Within goals, I have two options, time bounded goals or unbounded goals. Let me first think of an example for each so that it is easier to picture the difference.
I want to learn handstand this year before my birthday. I want to give it to myself as my birthday gift. Before 25 November 2026, I want to learn handstand. Where I am able to perform handstand for 30 seconds. This is time bounded goal.
I need to add time boundation for the things I care. Hence after that day, I must not get same amount of points. Points must decay. Like if I learn handstand before my birthday, I get 100 points, but after every week it is starts decaying at the rate of 1% per week ( I am not sure the exact number to use. Topic for later )
And if I don't do it suppose like for another 2 years, it still would be something I learned, just did not do it within the time, hence will still get some minimum points like 30.
So, after a point a time bounded goal can become same goal.

Example for unbounded goal is to run 5km in 30 min. This is something I would want to do, but I am fine If I don't do it. Hence, I will just set points to it, without time limit.
Thinking of it, I realized that I might also want to upgrade unbounded goal to to time bounded. For these I have two options, either to delete existing goal and create new with time bound or treat both of them as same in our code, hence we can just give option to use to just upgrade it as same goal becomes time bounded. Still not sure if to treat both of them different or same.

I think for now I can skip this point, because I will first implement the task logic in our code and will add support for Goals later. Since I know I will be treating both of them differently, I can work on them independently ( Another perk of keeping them separate )