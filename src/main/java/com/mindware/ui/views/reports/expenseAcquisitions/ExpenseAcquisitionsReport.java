package com.mindware.ui.views.reports.expenseAcquisitions;


import com.mindware.backend.rest.reports.ExpenseAcquisitionsRestTemplate;
import com.mindware.backend.util.UtilValues;
import com.mindware.ui.MainLayout;
import com.mindware.ui.components.FlexBoxLayout;
import com.mindware.ui.layout.size.Horizontal;
import com.mindware.ui.layout.size.Top;
import com.mindware.ui.util.UIUtils;
import com.mindware.ui.util.css.BoxSizing;
import com.mindware.ui.views.ViewFrame;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Route(value = "expense-acquisition-report", layout = MainLayout.class)
@ParentLayout(MainLayout.class)
@PageTitle("Reportes")

public class ExpenseAcquisitionsReport extends ViewFrame implements RouterLayout {

    private static final String EXPECTED_ACQUISITION_RESUME ="RESUMEN EJECUTIVO";
    private static final String ACQUISITION_DETAIL ="DETALLE ADQUISICIONES";
    private static final String BASICRECURRENT_DETAIL ="DETALLE CONTRATADO";

    @Autowired
    UtilValues utilValues;

    @Autowired
    private ExpenseAcquisitionsRestTemplate expenseAcquisitionsRestTemplate;

//    private ComboBox<String> periods;
    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        setViewContent(createContent());
    }

    private Accordion createOptionsReport(){
        Accordion optionDetails = new Accordion();

        AccordionPanel resumeExpenseAcquisitionPanel = optionDetails
                .add(EXPECTED_ACQUISITION_RESUME,layoutResumeExpenseAcquisition( "expense-acquisition","expense-acquisition-report"));
        AccordionPanel acquisitionDetailPanel = optionDetails
                .add(ACQUISITION_DETAIL,layoutResumeExpenseAcquisition("acquisition-detail","expense-acquisition-report"));
        AccordionPanel basicRecurrentDetailPanel = optionDetails
                .add(BASICRECURRENT_DETAIL,layoutResumeExpenseAcquisition("basicrecurrent-detail","expense-acquisition-report"));


        return optionDetails;
    }

    private HorizontalLayout layoutResumeExpenseAcquisition(String originReport, String pathPage){
        HorizontalLayout layout = new HorizontalLayout();

        ComboBox<String> periods = new ComboBox<>();
        periods.setWidth("200px");
        periods.setItems(utilValues.getAllPeriods());
        periods.setAllowCustomValue(false);
        periods.setClearButtonVisible(true);
        periods.setPlaceholder("Seleccione Periodo");

        Button print = new Button("Imprimir");
        print.addThemeVariants(ButtonVariant.LUMO_PRIMARY,ButtonVariant.LUMO_CONTRAST);
        print.setIcon(VaadinIcon.PRINT.create());
        print.addClickListener(event ->{
            if(periods.isEmpty()){
                UIUtils.showNotificationType("Seleccione Periodo para su impresión","alert");
                periods.focus();
                return;
            }
            Map<String, List<String>> paramExpenseAcquisition = new HashMap<>();
            List<String> origin = new ArrayList<>();
            origin.add(originReport);
            List<String> path = new ArrayList<>();
            path.add(pathPage);
            List<String> period = new ArrayList<>();
            period.add(periods.getValue());

            paramExpenseAcquisition.put("origin",origin);
            paramExpenseAcquisition.put("path",path);
            paramExpenseAcquisition.put("period",period);

            QueryParameters qp = new QueryParameters(paramExpenseAcquisition);
            UI.getCurrent().navigate("report-preview", qp);

        });




        layout.setSpacing(true);
        layout.add(periods,print);

        return layout;
    }


    private Component createContent(){
        FlexBoxLayout content = new FlexBoxLayout(createOptionsReport());
//        content.addClassName("grid-view");
        content.setHeightFull();
        content.setBoxSizing(BoxSizing.BORDER_BOX);
        content.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);

        return content;
    }
}
