package com.mindware.ui.views.reports.expenseServices;

import com.mindware.backend.entity.corebank.Concept;
import com.mindware.backend.rest.corebank.ConceptRestTemplate;
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
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

@Route(value = "expenses-services-report", layout = MainLayout.class)
@ParentLayout(MainLayout.class)
@PageTitle("Reportes")
public class ExpenseServicesReport extends ViewFrame implements RouterLayout {

    private static final String EXPENSE_SERVICE_BUDGET_RESUME ="RESUMEN EJECUTIVO SUCURSAL";
    private static final String EXPENSE_SERVICE_FATHER_BUSINESS_UNIT ="CONSOLIDADO SUCURSAL";
    private static final String EXPENSE_SERVICE_BUDGET_DETAIL ="DETALLE GASTOS SUCURSAL-AGENCIAS";

    @Autowired
    UtilValues utilValues;

    @Autowired
    ConceptRestTemplate conceptRestTemplate;

    private List<Concept> conceptList;

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        conceptList = new ArrayList<>(conceptRestTemplate.getSucursal());
        Concept concept = new Concept();
        concept.setCode("20");
        concept.setCode2("50");
        concept.setDescription("OFICINA NACIONAL");
        conceptList.add(concept);
        setViewContent(createContent());
    }

    private Accordion createOptionsReport() {
        Accordion optionDetails = new Accordion();

        AccordionPanel resumeExpenseServiceBudget = optionDetails
                .add(EXPENSE_SERVICE_BUDGET_RESUME, layoutExpenseServiceBusinessUnit( "expenses-service-resume-businessunit","expenses-services-report"));
        AccordionPanel expenseServiceBusinessUnit = optionDetails
                .add(EXPENSE_SERVICE_FATHER_BUSINESS_UNIT, layoutExpenseServiceBusinessUnit("expense-service-father-business-unit","expenses-services-report"));
        AccordionPanel expenseServiceBudgetDetail = optionDetails
                .add(EXPENSE_SERVICE_BUDGET_DETAIL, layoutExpenseServiceBusinessUnit("expenses-service-detail","expenses-services-report"));

        return optionDetails;
    }

    private HorizontalLayout layoutExpenseServiceBusinessUnit(String originReport, String pathPage){

        DatePicker cutOffDate = new DatePicker();
        cutOffDate.setWidth("300px");
        cutOffDate.setLocale(new Locale("es","BO"));
        cutOffDate.setClearButtonVisible(true);
        cutOffDate.setPlaceholder("Fecha corte");

        ComboBox<Concept> businessUnit = new ComboBox<>();
        businessUnit.setWidth("400px");
        businessUnit.setItems(conceptList);
        businessUnit.setItemLabelGenerator(Concept::getDescription);
        businessUnit.setPlaceholder("Seleccione Unidad de Negocio");

        Button print = new Button("Imprimir");
        print.addThemeVariants(ButtonVariant.LUMO_PRIMARY,ButtonVariant.LUMO_CONTRAST);
        print.setIcon(VaadinIcon.PRINT.create());
        print.addClickListener(event ->{

            Map<String, List<String>> paramExpensesServiceBudget = new HashMap<>();
            List<String> origin = new ArrayList<>();
            origin.add(originReport);
            List<String> path = new ArrayList<>();
            path.add(pathPage);



            if(originReport.equals("expenses-service-detail") || originReport.equals("expense-service-father-business-unit") || originReport.equals("expenses-service-resume-businessunit")){


                if (cutOffDate.isEmpty()) {
                    UIUtils.showNotificationType("Ingrese la Fecha de Corte", "alert");
                    cutOffDate.focus();
                    return;
                }
                if (businessUnit.isEmpty()) {
                    UIUtils.showNotificationType("Seleccione la Unidad de Negocio", "alert");
                    businessUnit.focus();
                    return;
                }

                List<String> cutOffDateParam = new ArrayList<>();
                cutOffDateParam.add(cutOffDate.getValue().toString());
                paramExpensesServiceBudget.put("cutoffdate",cutOffDateParam);

                List<String> year = new ArrayList<>();
                year.add(String.valueOf(cutOffDate.getValue().getYear()));

                List<String> codebusiness = new ArrayList<>();
                if(businessUnit.getValue().getDescription().equals("OFICINA NACIONAL")){
                    codebusiness.add(businessUnit.getValue().getCode());
                }else {
                    codebusiness.add(businessUnit.getValue().getCode());
                }

                List<String> nameBusinessUnit = new ArrayList<>();
                nameBusinessUnit.add(businessUnit.getValue().getDescription());
                paramExpensesServiceBudget.put("codefatherbusinessunit",codebusiness);
//                paramInvestmentBudget.put("year",year);
                paramExpensesServiceBudget.put("namebusinessunit",nameBusinessUnit);

            }
//            else if(originReport.equals("expense-service-father-business-unit")){
//                if (cutOffDate.isEmpty()) {
//                    UIUtils.showNotificationType("Ingrese la Fecha de Corte", "alert");
//                    cutOffDate.focus();
//                    return;
//                }
//
//                List<String> cutOffDateParam = new ArrayList<>();
//                cutOffDateParam.add(cutOffDate.getValue().toString());
//                paramExpensesServiceBudget.put("cutoffdate",cutOffDateParam);
//
//            }
            else if(originReport.equals("expenses-service-resume")){
                if (cutOffDate.isEmpty()) {
                    UIUtils.showNotificationType("Ingrese la Fecha de Corte", "alert");
                    cutOffDate.focus();
                    return;
                }

                List<String> cutOffDateParam = new ArrayList<>();
                cutOffDateParam.add(cutOffDate.getValue().toString());
                paramExpensesServiceBudget.put("cutoffdate",cutOffDateParam);

            }
            paramExpensesServiceBudget.put("origin",origin);
            paramExpensesServiceBudget.put("path",path);

            QueryParameters qp = new QueryParameters(paramExpensesServiceBudget);
            UI.getCurrent().navigate("report-preview", qp);

        });

        HorizontalLayout layout = new HorizontalLayout();
        layout.setSpacing(true);
        if(originReport.equals("expenses-service-detail") || originReport.equals("expense-service-father-business-unit") || originReport.equals("expenses-service-resume-businessunit")){
            layout.add( cutOffDate, businessUnit,print);
        }else {
            layout.add(cutOffDate, print);
        }
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
