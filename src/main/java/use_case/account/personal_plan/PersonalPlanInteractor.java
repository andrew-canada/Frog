package use_case.account.personal_plan;

import entity.User;
import use_case.gateway.UserDataAccessInterface;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;

import java.nio.file.Files;
import java.nio.file.Paths;

public final class PersonalPlanInteractor implements PersonalPlanInputBoundary {
    public static final String GEMINI_API_KEY_ENV = "GEMINI_API_KEY";

    private final UserDataAccessInterface users;
    private final PersonalPlanOutputBoundary presenter;

    public PersonalPlanInteractor(UserDataAccessInterface users, PersonalPlanOutputBoundary presenter) {
        this.users = users;
        this.presenter = presenter;
    }

    @Override public void execute(PersonalPlanInputData inputData) {

        User user = users.getCurrentUser().orElse(null);
        String calendar = getFile(inputData.calendarPath());

        if (user == null) {
            presenter.present(new PersonalPlanOutputData(false, "You need an account", ""));
        } else if (calendar == null) {
            presenter.present(new PersonalPlanOutputData(false, "Please upload a .ics file", ""));
        } else if (!isInt(inputData.nTrips())) {
            presenter.present(new PersonalPlanOutputData(false, "Please input an integer", ""));
        } else {
            String apiKey = System.getenv(GEMINI_API_KEY_ENV);
            if (apiKey == null || apiKey.isBlank()) {
                presenter.present(new PersonalPlanOutputData(false, "Set " + GEMINI_API_KEY_ENV + " to generate a personal plan", ""));
                return;
            }
            try {
                Client client = Client.builder().apiKey(apiKey).build();
                GenerateContentResponse response = client.models.generateContent("gemini-3.6-flash", "This is a UOFT time table, generate a schedule of washroom breaks such that there are " + inputData.nTrips() + "washroom trips: " + calendar, null);
                String personalPlan = response.text();
                users.removeUser(user.username());
                User newUser = new User(user.username(), user.passwordHash(), personalPlan);
                users.save(newUser);
                users.setCurrentUser(newUser);
                presenter.present(new PersonalPlanOutputData(true, "", personalPlan));
            } catch (Exception e) {
                System.out.println(e.getMessage());
                presenter.present(new PersonalPlanOutputData(false, "Try again", ""));
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

    private String getFile(String str) {

        try {
            return Files.readString(Paths.get(str));
        } catch (Exception e) {
            return null;
        }

    }

}
