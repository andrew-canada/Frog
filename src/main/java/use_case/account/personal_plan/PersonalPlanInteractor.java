package use_case.account.personal_plan;

import use_case.port.UserRepository;
import use_case.port.CurrentUserSession;
import entity.User;

public final class PersonalPlanInteractor implements PersonalPlanInputBoundary {
    private final UserRepository users;
    private final CurrentUserSession session;
    private final CalendarContentReader calendarReader;
    private final PersonalPlanGenerator generator;
    private final PersonalPlanOutputBoundary presenter;

    public PersonalPlanInteractor(UserRepository users, CurrentUserSession session, CalendarContentReader calendarReader,
                                  PersonalPlanGenerator generator, PersonalPlanOutputBoundary presenter) {
        this.users = users;
        this.session = session;
        this.calendarReader = calendarReader;
        this.generator = generator;
        this.presenter = presenter;
    }

    @Override
    public void execute(PersonalPlanInputData inputData) {

        User user = session.currentUser().orElse(null);
        String path = inputData.calendarPath();
        if (user == null) {
            presenter.present(new PersonalPlanOutputData(false, "You need an account", ""));
        } else if (!path.endsWith(".ics")) {
            presenter.present(new PersonalPlanOutputData(false, "Please upload a .ics file", ""));
        } else if (!isInt(inputData.nTrips())) {
            presenter.present(new PersonalPlanOutputData(false, "Please input an integer", ""));
        } else {
            try {
                String calendar = calendarReader.read(path);
                String personalPlanString = generator.generate(calendar, Integer.parseInt(inputData.nTrips()));
                users.removeUser(user.username());
                User newUser = new User(user.username(), user.passwordHash(), personalPlanString);
                users.save(newUser);
                session.setCurrentUser(newUser);
                presenter.present(new PersonalPlanOutputData(true, "", personalPlanString));
            } catch (Exception e) {
                presenter.present(new PersonalPlanOutputData(false, "Gemini error, please try again", ""));
            }
        }

    }

    private boolean isInt(String str) {

        try {
            Integer.parseInt(str);
            return true;
        } catch (Exception e) {
            return false;
        }

    }

}
