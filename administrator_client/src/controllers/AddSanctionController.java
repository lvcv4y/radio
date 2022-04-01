package controllers;

import coreclasses.RequestTypes;
import coreclasses.dataclasses.Request;
import coreclasses.dataclasses.SanctionEntity;
import coreclasses.dataclasses.UserEntity;
import coreclasses.thread_classes.TCPClient;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.ResourceBundle;

public class AddSanctionController implements Initializable {

    private final TCPClient client = TCPClient.getInstance();

    @FXML
    private TextArea description;

    @FXML
    private ChoiceBox<String> identifierType;

    @FXML
    private TextField identifierField;

    @FXML
    private ComboBox<Integer> startDay;

    @FXML
    private ComboBox<String> startMonth;

    @FXML
    private ComboBox<Integer> startYear;

    @FXML
    private ComboBox<Integer> startHour;

    @FXML
    private ComboBox<Integer> startMinute;

    @FXML
    private ComboBox<Integer> endDay;

    @FXML
    private ComboBox<String> endMonth;

    @FXML
    private ComboBox<Integer> endYear;

    @FXML
    private ComboBox<Integer> endHour;

    @FXML
    private ComboBox<Integer> endMinute;

    @FXML
    private Button addButton;

    @FXML
    private Button showDateStart;

    @FXML
    private Button showDateEnd;

    @FXML
    private ComboBox<String> type;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        setValues(startDay, 1, 31);
        setValues(endDay, 1, 31);

        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        setValues(startYear,currentYear, currentYear + 25);
        setValues(endYear, currentYear, currentYear + 26);

        setValues(startHour, 0, 23);
        setValues(endHour, 0, 23);

        setValues(startMinute, 0, 59);
        setValues(endMinute, 0, 59);

        showDateStart.setOnAction(e -> {
            final Calendar calendar = Calendar.getInstance();
            startDay.setValue(calendar.get(Calendar.DAY_OF_MONTH));
            startYear.setValue(calendar.get(Calendar.YEAR));
            startMonth.setValue(Months.getMonthById(calendar.get(Calendar.MONTH)).getName());
            startHour.setValue(calendar.get(Calendar.HOUR_OF_DAY));
            startMinute.setValue(calendar.get(Calendar.MINUTE));
        });

        showDateEnd.setOnAction(e -> {
            final Calendar calendar = Calendar.getInstance();
            endDay.setValue(calendar.get(Calendar.DAY_OF_MONTH));
            endYear.setValue(calendar.get(Calendar.YEAR));
            endMonth.setValue(Months.getMonthById(calendar.get(Calendar.MONTH)).getName());
            endHour.setValue(calendar.get(Calendar.HOUR_OF_DAY));
            endMinute.setValue(calendar.get(Calendar.MINUTE));
        });

        addButton.setOnAction(e -> {
            String desc = description.getText();
            String user = identifierField.getText();
            String idType = identifierType.getValue();
            String sanctionType = type.getValue();

            Integer sDay = startDay.getValue();
            String sMonth = startMonth.getValue();
            Integer sYear = startYear.getValue();
            Integer sHour = startHour.getValue();
            Integer sMinute = startMinute.getValue();

            Integer eDay = endDay.getValue();
            String eMonth = endMonth.getValue();
            Integer eYear = endYear.getValue();
            Integer eHour = endHour.getValue();
            Integer eMinute = endMinute.getValue();

            /*
             * NULL HANDLER
             * toutes les conditions servent a tester si une valeur est null ou vide (-> aucune valeur saisie)
             * Ultra répétitif, optimisation ? TODO
             */

            if (desc.isEmpty()) {
                showMissingArgError("description de la sanction");
                return;
            }

            if (user.isEmpty()) {
                showMissingArgError("identifieur (pseudo / id / ...)");
                return;
            }

            if (sDay == null) {
                showMissingArgError("jour du début de la sanction");
                return;
            }

            if (sMonth == null) {
                showMissingArgError("mois du début de la sanction");
                return;
            }

            if (sYear == null) {
                showMissingArgError("année du début de la sanction");
                return;
            }

            if (sHour == null) {
                showMissingArgError("heure du début de la sanction");
                return;
            }

            if (sMinute == null) {
                showMissingArgError("minute du début de la sanction");
                return;
            }

            if (eDay == null) {
                showMissingArgError("jour de la fin de la sanction");
                return;
            }

            if (eMonth == null) {
                showMissingArgError("mois de la fin de la sanction");
                return;
            }

            if (eYear == null) {
                showMissingArgError("année de la fin de la sanction");
                return;
            }

            if (eHour == null) {
                showMissingArgError("heure de la fin de la sanction");
                return;
            }

            if (eMinute == null) {
                showMissingArgError("minute de la fin de la sanction");
                return;
            }

            if(sanctionType == null){
                showMissingArgError("type de sanction");
                return;
            }

            // FIN TEST NULL

            // VERIFICATION DE LA VALIDITE DE LA DATE
            // TODO modifier directement les valeurs de startDay et endDay

            if (sMonth.equals(Months.FEVRIER.name) && ((sDay > 29) || (sDay == 29 && (sYear % 4 > 0)))){
                showCreateError("Le ".concat(String.valueOf(sDay)).concat(" Février ").concat(String.valueOf(sYear)).concat(" n'existe pas"));
                return;
            }

            if(eMonth.equals(Months.FEVRIER.name) && ((eDay > 29) || (eDay == 29 && (eYear % 4 > 0)))) {
                showCreateError("Le ".concat(String.valueOf(eDay)).concat(" Février ").concat(String.valueOf(eYear)).concat(" n'existe pas"));
                return;
            }

            if(sDay > 30 && Months.getMonthByName(sMonth).id % 2 > 0) {
                showCreateError("Le ".concat(String.valueOf(sDay)).concat(" ")
                        .concat(Months.getMonthByName(sMonth).value).concat(" ")
                        .concat(String.valueOf(sYear)).concat("(date de début entrée) n'exite pas"));
                return;
            }

            if(eDay > 30 && Months.getMonthByName(eMonth).id % 2 > 0) {
                showCreateError("Le ".concat(String.valueOf(eDay)).concat(" ")
                        .concat(Months.getMonthByName(eMonth).value).concat(" ")
                        .concat(String.valueOf(eYear)).concat("(date de fin entrée) n'exite pas"));
                return;
            }

            // creation des objets Date

            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");


            try {
                Date startDate = format.parse(String.valueOf(sDay).concat("/")
                        .concat(String.valueOf(Months.getMonthByName(sMonth).getId() + 1)).concat("/")
                        .concat(String.valueOf(sYear)).concat(" ").concat(String.valueOf(sHour)).concat(":")
                        .concat(String.valueOf(sMinute)));

                Date endDate = format.parse(String.valueOf(eDay).concat("/")
                        .concat(String.valueOf(Months.getMonthByName(eMonth).getId() + 1)).concat("/")
                        .concat(String.valueOf(eYear)).concat(" ").concat(String.valueOf(eHour)).concat(":")
                        .concat(String.valueOf(eMinute)));

                System.out.println(format.format(startDate));
                System.out.println(format.format(endDate));

                final UserEntity entity;
                switch(idType){
                    case "PSEUDO":
                        entity = new UserEntity(null, null, user, null, null);
                        break;
                    case "EMAIL":
                        entity = new UserEntity(null, user, null, null, null);
                        break;
                    case "ID":
                        entity = new UserEntity(Integer.valueOf(user), null, null, null, null);
                        break;

                    default:
                        entity = null;
                        break;
                }

                if(entity == null){
                    showCreateError("L'identifieur reçu est n'a pas été reconnu (impossible de savoir s'il s'agit d'un email, d'un pseudo ou d'un id). " +
                            "Sois vous me virez, sois vous me le signalez, à vous de choisir.");
                    return;
                }

                addSanction(new SanctionEntity(sanctionType, entity, desc, startDate, endDate));

            } catch (ParseException pe) {
                String stackTrace = "";
                for(StackTraceElement ste : pe.getStackTrace()){
                    stackTrace = stackTrace.concat(ste.toString()).concat("\n");
                }

                showCreateError("Erreur lors de la création des objets correspondant au date (ParseException). " +
                        "Veuillez m'envoyer le rapport de l'erreur sur Discord (@Erispar#5258)\n#\n#\n#\n".concat(stackTrace));
            }
        });
    }

    private void setValues(ComboBox<Integer> intComboBox, int startValue, int endValue) throws IllegalArgumentException {

        if(intComboBox == null || startValue > endValue){
            throw new IllegalArgumentException(intComboBox == null ? "Combo box is null" : "start value greater than end value");
        }

        ObservableList<Integer> list = FXCollections.observableArrayList();
        for(int i = startValue; i <= endValue; i++){
            list.add(i);
        }

        intComboBox.setItems(list);
    }

    private void showMissingArgError(String argName){
        showCreateError("Un argument est manquant ! (".concat(argName).concat("). Si vous avez un doute sur " +
                "l'utilitation d'un argument, contactez moi (@Erispar#5258 sur Discord) ou demandez à un autre administrateur"));
    }

    private void showCreateError(String desc){
        AlertBox.showErrorBox("Erreur - ajout d'une sanction", "Erreur lors de la création de la sanction", desc);
    }

    private void addSanction(SanctionEntity entity){
        final Request r;
        r = new Request(RequestTypes.ADD_SANCTION.getName());
        r.addSanctionArgs(entity);
        client.query(r);
    }

    private enum Months {
        JANVIER("JANVIER", 0, "Janvier"), FEVRIER("FEVRIER",1, "Février"), MARS("MARS", 2, "Mars"),
        AVRIL("AVRIL", 3, "Avril"), MAI("MAI", 4, "Mai"), JUIN("JUIN", 5, "Juin"),
        JUILLET("JUILLET", 6, "Juillet"), AOUT("AOUT", 7, "Août"), SEPTEMBRE("SEPTEMBRE", 8, "Septembre"),
        OCTOBRE("OCTOBRE", 9, "Octobre"), NOVEMBRE("NOVEMBRE", 10, "Novembre"), DECEMBRE("DECEMBRE", 11, "Décembre");

        private final String name;
        private final int id;
        private final String value;

        Months(String name, int id, String value) {
            this.name = name;
            this.id = id;
            this.value = value;
        }

        private String getName(){return this.name; }
        private int getId(){return this.id;}
        private String getValue(){return this.value; }

        public static Months getMonthByName(String n){
            for(Months month : Months.values()){
                if(month.name.equals(n))
                    return month;
            }

            return null;
        }

        public static Months getMonthById(int i){
            for(Months month : Months.values()){
                if(month.id == i){
                    return month;
                }
            }

            return null;
        }
    }
}
