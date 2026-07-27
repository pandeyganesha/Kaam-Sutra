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