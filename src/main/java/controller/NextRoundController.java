package controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.FightDrawing.FightDrawStrategyPicker;
import model.WeaponCompetition;
import model.config.ConfigReader;
import model.enums.WeaponType;
import util.Pointer;
import java.net.URL;
import java.util.ResourceBundle;

public class NextRoundController implements Initializable {


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ;
    }
    @FXML
    Spinner<Integer> groupSize;
    @FXML
    Spinner<Integer> participantsCount;
    @FXML
    ChoiceBox strategyChooser;
    @FXML
    Button okButton;
    @FXML
    Button cancelButton;
    @FXML
    CheckBox fSemiFinal;

    private int iGroupSize=0;
    private int iParticipantsCount=0;
    private Pointer<WeaponCompetition.RoundCreator> out;
    private WeaponCompetition wc;

    public void setData(WeaponCompetition wc, Pointer<WeaponCompetition.RoundCreator> p)
    {
        out=p;
        this.wc=wc;
        iGroupSize  = ConfigReader.getInstance().getIntValue(WeaponType.str(wc.getWeaponType())+"_ROUND_"+Integer.toString(wc.getNextRoundNumber()),"GROUP_SIZE",0);
        iParticipantsCount= ConfigReader.getInstance().getIntValue(WeaponType.str(wc.getWeaponType())+"_ROUND_"+Integer.toString(wc.getNextRoundNumber()),"PARTICIPANTS_COUNT",0);
        update();
    }


    private void update()
    {
        // causes nullptr exception. find how to set default number to sinner
        groupSize.getValueFactory().setValue(new Integer(iGroupSize));
        participantsCount.getValueFactory().setValue(iParticipantsCount);
        okButton.setOnAction(a ->
        {
            System.out.println("set next round");
            iGroupSize=groupSize.getValue();
            iParticipantsCount=participantsCount.getValue();
            WeaponCompetition.RoundCreator rc = wc.prepareNewRound(iGroupSize,iParticipantsCount,fSemiFinal.isSelected(),FightDrawStrategyPicker.STRATEGY_NAMES.fromString((String)strategyChooser.getValue()));
            out.set(rc);
            Stage toClose = (Stage) okButton.getScene().getWindow();
            toClose.close();
        });
        cancelButton.setOnAction(a-> {
            System.out.println("set next round");
            out.set(null);

            Stage toClose = (Stage) cancelButton.getScene().getWindow();
            toClose.close();
        });
        strategyChooser.setItems(FXCollections.observableArrayList(
                FightDrawStrategyPicker.STRATEGY_NAMES.listAllShowedString())
        );
    }

    @FXML
    private void semiFinalCheckboxAction(){
        boolean fDisable = fSemiFinal.isSelected();
        groupSize.setDisable(fDisable);
        participantsCount.setDisable(fDisable);
        strategyChooser.setDisable(fDisable);
        groupSize.getValueFactory().setValue(2);
        participantsCount.getValueFactory().setValue(4);
    }




}
