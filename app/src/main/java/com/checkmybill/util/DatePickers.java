package com.checkmybill.util;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.NumberPicker;

import com.checkmybill.R;

import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Petrus A. (R@G3), ESPE... On 09/12/2016.
 */

public class DatePickers {
    // -> Classe para trabalhar no modo 'mm-yyyy'
    public static class MonthAndYearDatePicker {
        final private static String TAG = "MonthAndYearDatePicker";

        private Activity activity;
        private View baseView;
        private NumberPicker monthNumPicker;
        private NumberPicker yearNumPicker;

        private String[] monthNames = new String[12];
        AlertDialog.Builder builder;

        private MonthAndYearDatePickerInterface.OnPositiveEvent positiveEvent;
        private MonthAndYearDatePickerInterface.OnNegativeEvent negativeEvent;

        public MonthAndYearDatePicker(Activity activity) {
            Calendar c = Calendar.getInstance();
            this.activity = activity;

            // Alimentando a lista de nomes do mes
            DateFormatSymbols dfs = new DateFormatSymbols();
            monthNames = dfs.getMonths();

            // Obtendo elementos de tela
            final LayoutInflater inflater = this.activity.getLayoutInflater();
            this.baseView = inflater.inflate(R.layout.datepicker_monthyear_layout, null);
            this.monthNumPicker = (NumberPicker) baseView.findViewById(R.id.monthNumPicker);
            this.yearNumPicker = (NumberPicker) baseView.findViewById(R.id.yearNumPicker);

            // Definindo os limities dos elementos
            // -> Ano
            yearNumPicker.setMinValue(c.get(Calendar.YEAR) - 10);
            yearNumPicker.setMaxValue(c.get(Calendar.YEAR) + 10);
            yearNumPicker.setValue( c.get(Calendar.YEAR) );
            // -> Mes
            monthNumPicker.setMinValue(0);
            monthNumPicker.setMaxValue(11);
            monthNumPicker.setValue( c.get(Calendar.MONTH) );
            monthNumPicker.setDisplayedValues( monthNames );

            // Inicializando eventos
            positiveEvent = new MonthAndYearDatePickerInterface.OnPositiveEvent() {
                @Override
                public void onEvent(Date date, DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            };
            negativeEvent = new MonthAndYearDatePickerInterface.OnNegativeEvent() {
                @Override
                public void onEvent(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            };

            // Criando builder do dialogBox
            builder = new AlertDialog.Builder(activity);
            builder.setView(baseView);
            builder.setCancelable(false);
            builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    negativeEvent.onEvent(dialogInterface, i);
                }
            });
            builder.setPositiveButton("Definir", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    SimpleDateFormat sdf = new SimpleDateFormat("MM-yyyy");
                    final String strDate = (1 + monthNumPicker.getValue()) + "-" + yearNumPicker.getValue();
                    try {
                        Date date = sdf.parse( strDate );
                        Log.d(TAG, "Selected Date: -> " + date.toString());
                        positiveEvent.onEvent(date, dialogInterface, i);
                    } catch (ParseException e) {
                        Log.e(TAG, "Fatal: Date Exception: -> " + Util.getMessageErrorFromExcepetion(e));
                    }
                }
            });
        }

        public void setInitialDateRangeValue(Date initialDate) {
            Calendar c = Calendar.getInstance();
            c.setTime(initialDate);

            yearNumPicker.setValue(c.get(Calendar.YEAR));
            monthNumPicker.setValue(c.get(Calendar.MONTH));
        }

        public void setPositiveEvent(MonthAndYearDatePickerInterface.OnPositiveEvent positiveEvent) {
            this.positiveEvent = positiveEvent;
        }

        public void setNegativeEvent(MonthAndYearDatePickerInterface.OnNegativeEvent negativeEvent) {
            this.negativeEvent = negativeEvent;
        }

        public void setTitle(final String title) {
            builder.setTitle(title);
        }

        public void showDialogWindow() {
            Log.d(TAG, "Showing MonthYearDatePicker");
            builder.create().show();
        }
    }

    // -> Classe para trabalhar com range da data (Formato: dd-mm-yyyy)
    public static class DateRangeDatePicker {
        final private static String TAG = "DateRangeDatePicker";
        private Activity activity;
        private View pickerLayout;
        private DatePicker dtPicker1;
        private DatePicker dtPicker2;
        private AlertDialog.Builder builder;

        DateRangeDatePickerInterface.OnNegativeEvent negativeEvent;
        DateRangeDatePickerInterface.OnPositiveEvent positiveEvent;

        public DateRangeDatePicker(Activity activity) {
            this.activity = activity;
            this.pickerLayout = this.activity.getLayoutInflater().inflate(R.layout.datepicker_rangedate_layout, null);
            this.dtPicker1 = (DatePicker) pickerLayout.findViewById(R.id.datePicker1);
            this.dtPicker2 = (DatePicker) pickerLayout.findViewById(R.id.datePicker2);

            // Cirando o dialog
            builder = new AlertDialog.Builder(activity);
            builder.setCancelable(false);
            builder.setView( pickerLayout );

            // Criando eventos
            positiveEvent = new DateRangeDatePickerInterface.OnPositiveEvent() {
                @Override
                public void onEvent(Date firstDate, Date secondDate, DialogInterface dialogInterface, int i) {
                }
            };
            negativeEvent = new DateRangeDatePickerInterface.OnNegativeEvent() {
                @Override
                public void onEvent(DialogInterface dialogInterface, int i) {
                }
            };
            builder.setPositiveButton("Definir", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Date date1 = new Date(dtPicker1.getYear() - 1900, dtPicker1.getMonth(), dtPicker1.getDayOfMonth());
                    Date date2 = new Date(dtPicker2.getYear() - 1900, dtPicker2.getMonth(), dtPicker2.getDayOfMonth());
                    // Ordenando as dados (patra que a menor, seja a variavel 'date1'
                    if ( date2.compareTo(date1) < 0 ) {
                        Date tmp = date2;
                        date2 = date1;
                        date1 = tmp;
                    }

                    Log.d(TAG, "Selected Dates: -> " + date1.toString() + " - " + date2.toString());
                    positiveEvent.onEvent(date1, date2, dialogInterface, i);
                }
            });
            builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    negativeEvent.onEvent(dialogInterface, i);
                }
            });
        }

        public void setInitialDateRangeValues(final Date startDate, final Date endDate) {
            Log.d(TAG, "Upadating range date to -> " + startDate.toString() + " - " + endDate.toString());

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(startDate);
            dtPicker1.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

            calendar.setTime(endDate);
            dtPicker2.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        }

        public void setNegativeEvent(DateRangeDatePickerInterface.OnNegativeEvent negativeEvent) {
            this.negativeEvent = negativeEvent;
        }

        public void setPositiveEvent(DateRangeDatePickerInterface.OnPositiveEvent positiveEvent) {
            this.positiveEvent = positiveEvent;
        }

        public void setTitle(final String title) {
            builder.setTitle(title);
        }

        public void showDialogWindow() {
            Log.d(TAG, "Showing RangeDatePicker");
            builder.create().show();
        }
    }

    // ---------------------------------------------------------------------------------------------
    // INTERFACES DE EVENTOS RELACIONADOS AO DATEPICKERS...
    public interface MonthAndYearDatePickerInterface {
        interface OnPositiveEvent {
            void onEvent(Date date, DialogInterface dialogInterface, int i);
        }
        interface OnNegativeEvent {
            void onEvent(DialogInterface dialogInterface, int i);
        }
    }

    public interface  DateRangeDatePickerInterface {
        interface OnPositiveEvent {
            void onEvent(Date firstDate, Date secondDate, DialogInterface dialogInterface, int i);
        }
        interface OnNegativeEvent {
            void onEvent(DialogInterface dialogInterface, int i);
        }
    }
}
