/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chaudieregvb7_swing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TooManyListenersException;
import java.util.logging.Level;
import java.util.logging.Logger;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;
import java.awt.Color;
import java.awt.Font;
import java.time.LocalDateTime;
import javax.swing.JLabel;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
/**
 *
 * @author user
 */
public class NewJFrame extends javax.swing.JFrame { //implements SerialPortEventListener {

    /**
     * Creates new form NewJFrame
     */
    PanelConsigne[] _pnlConsignes = new PanelConsigne[24];
    public int _heureCourante = 0;
    public NewJFrame() {
        initComponents();
        jTable1.getTableHeader().setFont(new Font("Serif", Font.BOLD,14));
        DefaultTableCellRenderer cr = new DefaultTableCellRenderer();
        cr.setHorizontalAlignment(JLabel.CENTER);
        cr.setVerticalAlignment(JLabel.TOP);
        jTable1.getColumnModel().getColumn(0).setCellRenderer(cr);
        int nb_heures = 24;
        panelConsigne1._h = 0;
        panelConsigne2._h = 1;
        panelConsigne3._h = 2;
        panelConsigne4._h = 3;
        panelConsigne5._h = 4;
        panelConsigne6._h = 5;
        panelConsigne7._h = 6;
        panelConsigne8._h = 7;
        panelConsigne9._h = 8;
        panelConsigne10._h = 9;
        panelConsigne11._h = 10;
        panelConsigne12._h = 11;
        panelConsigne13._h = 12;
        panelConsigne14._h = 13;
        panelConsigne15._h = 14;
        panelConsigne16._h = 15;
        panelConsigne17._h = 16;
        panelConsigne18._h = 17;
        panelConsigne19._h = 18;
        panelConsigne20._h = 19;
        panelConsigne21._h = 20;
        panelConsigne22._h = 21;
        panelConsigne23._h = 22;
        panelConsigne24._h = 23;
        _consignes = new int[nb_heures];
        SetConsignes("TRAVAIL 1");


        // Communication avec l'Arduino
        // ****************************
        try {
            //CommPortIdentifier portId = CommPortIdentifier.getPortIdentifier("COM4");//"/dev/ttyUSB0");
            //serialPort = (SerialPort) portId.open("Demo application", 5000);
            connect("/dev/ttyACM0");
        } catch (NoSuchPortException | PortInUseException | IOException | UnsupportedCommOperationException | TooManyListenersException ex) {
           System.out.println("*** Exception dans Communication avec l'Arduino");
        }
        
        // Timer pour envoyer les consignes
        task = new TimerTask()
        {
            @Override
            public void run()
                {
                    Calendar calendar = Calendar.getInstance();
                    int heure = calendar.get(Calendar.HOUR_OF_DAY);

                    //if(_heureCourante != heure) {
                        _heureCourante = heure;
                        //DefaultTableModel model = (DefaultTableModel)jTable1.getModel();
                        jTable1.changeSelection(0, heure,true, false);
                        //model.setValueAt("O",0,0);
                    //}
                    
                    
                    if(timerState == 1) {
                        //Envoi de la consigne a l'arduino
                        int consigne = _consignes[heure];
                        if(consigne != _old_consigne) {
                            System.out.println("Heure=" + heure + ", Consigne=" + consigne );
                            EnvoiConsigne(consigne);
                            _old_consigne = consigne;
                        }
                    }
                    if(timerState == 0) {
                        //Envoi de la consigne a l'arduino
                        if(0 != _old_consigne) {
                            System.out.println("Heure=" + heure + ", Consigne=0" );
                            EnvoiConsigne(0);
                            _old_consigne = 0;
                        }
                    }
                    if(timerState == 2) { // RESTART chaudiere
                        //Envoi de la consigne a l'arduino
                        if(1 != _old_consigne) {
                            System.out.println("Heure=" + heure + ", Consigne=1" );
                            EnvoiConsigne(1);
                            _old_consigne = 1;
                            timerState = 1;
                        }
                    }
                    
                    
            // Read msg from arduino
            try {
                if(input.ready()) {
                //boolean hasData = false;
                //do {
                //    hasData = false;
                    String inputLine = input.readLine(); // BLOQUAIT PARFOIS
                    if(inputLine != null && inputLine.length() > 0) {
                        txt_service.append( inputLine + "\n" );
                        if(NOScroll.isSelected() == false ) {
                            txt_service.setCaretPosition(txt_service.getText().length());
                        }
                        // Si trop de msg vide la textarea
                        if(txt_service.getText().length() >= 16383) {
                            txt_service.setText("");
 
                        }
                    }
                //} while(hasData == true);
                    //System.out.println(inputLine);
            }
            } catch (Exception e) {
                    System.out.println("*** Exception dans serialEvent)");
                    System.err.println(e.toString());
            }
                }

        };
        timer.schedule(task,5000l, 100l); // Lance le timer après 5 sec puis s'execute toutes les 1 sec (6 000)

        
    }
    int _old_consigne = 0;
    public void EnvoiConsigne(int c) {
        System.out.println("EnvoiConsigne a "+LocalDateTime.now().getHour()+"h"+LocalDateTime.now().getMinute()+"m"+LocalDateTime.now().getSecond()+"s = "+c);

        try {
                outStream.write(c);
            } catch (IOException ex) {
                System.out.println("*** Exception dans TimerTask::run");
            }
        System.out.println("FIN EnvoiConsigne a "+LocalDateTime.now().getHour()+"h"+LocalDateTime.now().getMinute()+"m"+LocalDateTime.now().getSecond()+"s");
    }
    // Timer
    Timer timer = new Timer();
    TimerTask task = null;
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox<>();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jPanelConsignes = new javax.swing.JPanel();
        panelConsigne1 = new chaudieregvb7_swing.PanelConsigne();
        panelConsigne2 = new chaudieregvb7_swing.PanelConsigne();
        panelConsigne3 = new chaudieregvb7_swing.PanelConsigne();
        panelConsigne4 = new chaudieregvb7_swing.PanelConsigne();
        panelConsigne5 = new chaudieregvb7_swing.PanelConsigne();
        panelConsigne6 = new chaudieregvb7_swing.PanelConsigne();
        panelConsigne7 = new chaudieregvb7_swing.PanelConsigne();
        panelConsigne8 = new chaudieregvb7_swing.PanelConsigne();
        panelConsigne9 = new chaudieregvb7_swing.PanelConsigne();
        panelConsigne10 = new chaudieregvb7_swing.PanelConsigne();
        panelConsigne11 = new chaudieregvb7_swing.PanelConsigne();
        panelConsigne12 = new chaudieregvb7_swing.PanelConsigne();
        panelConsigne13 = new chaudieregvb7_swing.PanelConsigne();
        panelConsigne14 = new chaudieregvb7_swing.PanelConsigne();
        panelConsigne15 = new chaudieregvb7_swing.PanelConsigne();
        panelConsigne16 = new chaudieregvb7_swing.PanelConsigne();
        panelConsigne17 = new chaudieregvb7_swing.PanelConsigne();
        panelConsigne18 = new chaudieregvb7_swing.PanelConsigne();
        panelConsigne19 = new chaudieregvb7_swing.PanelConsigne();
        panelConsigne20 = new chaudieregvb7_swing.PanelConsigne();
        panelConsigne21 = new chaudieregvb7_swing.PanelConsigne();
        panelConsigne22 = new chaudieregvb7_swing.PanelConsigne();
        panelConsigne23 = new chaudieregvb7_swing.PanelConsigne();
        panelConsigne24 = new chaudieregvb7_swing.PanelConsigne();
        jPanel3 = new javax.swing.JPanel();
        jButtonON = new javax.swing.JButton();
        jButtonOFF = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        txt_service = new javax.swing.JTextArea();
        NOScroll = new javax.swing.JToggleButton();
        jButton1 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jTabbedPane1.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N

        jLabel1.setFont(new java.awt.Font("Tahoma", 0, 36)); // NOI18N
        jLabel1.setText("PROGRAMME :");

        jComboBox1.setFont(new java.awt.Font("Tahoma", 0, 36)); // NOI18N
        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "TRAVAIL 1", "TRAVAIL 2", "PRESENCE", "HORS GEL", "ZERO", "MAXIMUM" }));
        jComboBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox1ActionPerformed(evt);
            }
        });

        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        jTable1.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {"", "", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable1.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        jTable1.setAutoscrolls(false);
        jTable1.setCellSelectionEnabled(true);
        jTable1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jTable1.setIntercellSpacing(new java.awt.Dimension(0, 0));
        jTable1.setRowHeight(48);
        jTable1.setSelectionBackground(new java.awt.Color(0, 0, 204));
        jTable1.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jTable1.setShowHorizontalLines(false);
        jTable1.getTableHeader().setResizingAllowed(false);
        jTable1.getTableHeader().setReorderingAllowed(false);
        jScrollPane1.setViewportView(jTable1);
        jTable1.getAccessibleContext().setAccessibleParent(jTable1);

        jPanelConsignes.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 0, 0));

        panelConsigne1.setMinimumSize(new java.awt.Dimension(34, 308));
        panelConsigne1.setPreferredSize(new java.awt.Dimension(35, 400));

        javax.swing.GroupLayout panelConsigne1Layout = new javax.swing.GroupLayout(panelConsigne1);
        panelConsigne1.setLayout(panelConsigne1Layout);
        panelConsigne1Layout.setHorizontalGroup(
            panelConsigne1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 33, Short.MAX_VALUE)
        );
        panelConsigne1Layout.setVerticalGroup(
            panelConsigne1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 398, Short.MAX_VALUE)
        );

        jPanelConsignes.add(panelConsigne1);

        panelConsigne2.setPreferredSize(new java.awt.Dimension(35, 400));

        javax.swing.GroupLayout panelConsigne2Layout = new javax.swing.GroupLayout(panelConsigne2);
        panelConsigne2.setLayout(panelConsigne2Layout);
        panelConsigne2Layout.setHorizontalGroup(
            panelConsigne2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 33, Short.MAX_VALUE)
        );
        panelConsigne2Layout.setVerticalGroup(
            panelConsigne2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 398, Short.MAX_VALUE)
        );

        jPanelConsignes.add(panelConsigne2);

        panelConsigne3.setPreferredSize(new java.awt.Dimension(35, 400));

        javax.swing.GroupLayout panelConsigne3Layout = new javax.swing.GroupLayout(panelConsigne3);
        panelConsigne3.setLayout(panelConsigne3Layout);
        panelConsigne3Layout.setHorizontalGroup(
            panelConsigne3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 33, Short.MAX_VALUE)
        );
        panelConsigne3Layout.setVerticalGroup(
            panelConsigne3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 398, Short.MAX_VALUE)
        );

        jPanelConsignes.add(panelConsigne3);

        panelConsigne4.setPreferredSize(new java.awt.Dimension(35, 400));

        javax.swing.GroupLayout panelConsigne4Layout = new javax.swing.GroupLayout(panelConsigne4);
        panelConsigne4.setLayout(panelConsigne4Layout);
        panelConsigne4Layout.setHorizontalGroup(
            panelConsigne4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 33, Short.MAX_VALUE)
        );
        panelConsigne4Layout.setVerticalGroup(
            panelConsigne4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 398, Short.MAX_VALUE)
        );

        jPanelConsignes.add(panelConsigne4);

        panelConsigne5.setPreferredSize(new java.awt.Dimension(35, 400));

        javax.swing.GroupLayout panelConsigne5Layout = new javax.swing.GroupLayout(panelConsigne5);
        panelConsigne5.setLayout(panelConsigne5Layout);
        panelConsigne5Layout.setHorizontalGroup(
            panelConsigne5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 33, Short.MAX_VALUE)
        );
        panelConsigne5Layout.setVerticalGroup(
            panelConsigne5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 398, Short.MAX_VALUE)
        );

        jPanelConsignes.add(panelConsigne5);

        panelConsigne6.setPreferredSize(new java.awt.Dimension(35, 400));

        javax.swing.GroupLayout panelConsigne6Layout = new javax.swing.GroupLayout(panelConsigne6);
        panelConsigne6.setLayout(panelConsigne6Layout);
        panelConsigne6Layout.setHorizontalGroup(
            panelConsigne6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 33, Short.MAX_VALUE)
        );
        panelConsigne6Layout.setVerticalGroup(
            panelConsigne6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 398, Short.MAX_VALUE)
        );

        jPanelConsignes.add(panelConsigne6);

        panelConsigne7.setPreferredSize(new java.awt.Dimension(35, 400));

        javax.swing.GroupLayout panelConsigne7Layout = new javax.swing.GroupLayout(panelConsigne7);
        panelConsigne7.setLayout(panelConsigne7Layout);
        panelConsigne7Layout.setHorizontalGroup(
            panelConsigne7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 33, Short.MAX_VALUE)
        );
        panelConsigne7Layout.setVerticalGroup(
            panelConsigne7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 398, Short.MAX_VALUE)
        );

        jPanelConsignes.add(panelConsigne7);

        panelConsigne8.setPreferredSize(new java.awt.Dimension(35, 400));

        javax.swing.GroupLayout panelConsigne8Layout = new javax.swing.GroupLayout(panelConsigne8);
        panelConsigne8.setLayout(panelConsigne8Layout);
        panelConsigne8Layout.setHorizontalGroup(
            panelConsigne8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 33, Short.MAX_VALUE)
        );
        panelConsigne8Layout.setVerticalGroup(
            panelConsigne8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 398, Short.MAX_VALUE)
        );

        jPanelConsignes.add(panelConsigne8);

        panelConsigne9.setPreferredSize(new java.awt.Dimension(35, 400));

        javax.swing.GroupLayout panelConsigne9Layout = new javax.swing.GroupLayout(panelConsigne9);
        panelConsigne9.setLayout(panelConsigne9Layout);
        panelConsigne9Layout.setHorizontalGroup(
            panelConsigne9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 33, Short.MAX_VALUE)
        );
        panelConsigne9Layout.setVerticalGroup(
            panelConsigne9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 398, Short.MAX_VALUE)
        );

        jPanelConsignes.add(panelConsigne9);

        panelConsigne10.setPreferredSize(new java.awt.Dimension(35, 400));

        javax.swing.GroupLayout panelConsigne10Layout = new javax.swing.GroupLayout(panelConsigne10);
        panelConsigne10.setLayout(panelConsigne10Layout);
        panelConsigne10Layout.setHorizontalGroup(
            panelConsigne10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 33, Short.MAX_VALUE)
        );
        panelConsigne10Layout.setVerticalGroup(
            panelConsigne10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 398, Short.MAX_VALUE)
        );

        jPanelConsignes.add(panelConsigne10);

        panelConsigne11.setPreferredSize(new java.awt.Dimension(35, 400));

        javax.swing.GroupLayout panelConsigne11Layout = new javax.swing.GroupLayout(panelConsigne11);
        panelConsigne11.setLayout(panelConsigne11Layout);
        panelConsigne11Layout.setHorizontalGroup(
            panelConsigne11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 33, Short.MAX_VALUE)
        );
        panelConsigne11Layout.setVerticalGroup(
            panelConsigne11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 398, Short.MAX_VALUE)
        );

        jPanelConsignes.add(panelConsigne11);

        panelConsigne12.setPreferredSize(new java.awt.Dimension(35, 400));

        javax.swing.GroupLayout panelConsigne12Layout = new javax.swing.GroupLayout(panelConsigne12);
        panelConsigne12.setLayout(panelConsigne12Layout);
        panelConsigne12Layout.setHorizontalGroup(
            panelConsigne12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 33, Short.MAX_VALUE)
        );
        panelConsigne12Layout.setVerticalGroup(
            panelConsigne12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 398, Short.MAX_VALUE)
        );

        jPanelConsignes.add(panelConsigne12);

        panelConsigne13.setPreferredSize(new java.awt.Dimension(35, 400));

        javax.swing.GroupLayout panelConsigne13Layout = new javax.swing.GroupLayout(panelConsigne13);
        panelConsigne13.setLayout(panelConsigne13Layout);
        panelConsigne13Layout.setHorizontalGroup(
            panelConsigne13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 33, Short.MAX_VALUE)
        );
        panelConsigne13Layout.setVerticalGroup(
            panelConsigne13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 398, Short.MAX_VALUE)
        );

        jPanelConsignes.add(panelConsigne13);

        panelConsigne14.setPreferredSize(new java.awt.Dimension(35, 400));

        javax.swing.GroupLayout panelConsigne14Layout = new javax.swing.GroupLayout(panelConsigne14);
        panelConsigne14.setLayout(panelConsigne14Layout);
        panelConsigne14Layout.setHorizontalGroup(
            panelConsigne14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 33, Short.MAX_VALUE)
        );
        panelConsigne14Layout.setVerticalGroup(
            panelConsigne14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 398, Short.MAX_VALUE)
        );

        jPanelConsignes.add(panelConsigne14);

        panelConsigne15.setPreferredSize(new java.awt.Dimension(35, 400));

        javax.swing.GroupLayout panelConsigne15Layout = new javax.swing.GroupLayout(panelConsigne15);
        panelConsigne15.setLayout(panelConsigne15Layout);
        panelConsigne15Layout.setHorizontalGroup(
            panelConsigne15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 33, Short.MAX_VALUE)
        );
        panelConsigne15Layout.setVerticalGroup(
            panelConsigne15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 398, Short.MAX_VALUE)
        );

        jPanelConsignes.add(panelConsigne15);

        panelConsigne16.setPreferredSize(new java.awt.Dimension(35, 400));

        javax.swing.GroupLayout panelConsigne16Layout = new javax.swing.GroupLayout(panelConsigne16);
        panelConsigne16.setLayout(panelConsigne16Layout);
        panelConsigne16Layout.setHorizontalGroup(
            panelConsigne16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 33, Short.MAX_VALUE)
        );
        panelConsigne16Layout.setVerticalGroup(
            panelConsigne16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 398, Short.MAX_VALUE)
        );

        jPanelConsignes.add(panelConsigne16);

        panelConsigne17.setPreferredSize(new java.awt.Dimension(35, 400));

        javax.swing.GroupLayout panelConsigne17Layout = new javax.swing.GroupLayout(panelConsigne17);
        panelConsigne17.setLayout(panelConsigne17Layout);
        panelConsigne17Layout.setHorizontalGroup(
            panelConsigne17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 33, Short.MAX_VALUE)
        );
        panelConsigne17Layout.setVerticalGroup(
            panelConsigne17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 398, Short.MAX_VALUE)
        );

        jPanelConsignes.add(panelConsigne17);

        panelConsigne18.setPreferredSize(new java.awt.Dimension(35, 400));

        javax.swing.GroupLayout panelConsigne18Layout = new javax.swing.GroupLayout(panelConsigne18);
        panelConsigne18.setLayout(panelConsigne18Layout);
        panelConsigne18Layout.setHorizontalGroup(
            panelConsigne18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 33, Short.MAX_VALUE)
        );
        panelConsigne18Layout.setVerticalGroup(
            panelConsigne18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 398, Short.MAX_VALUE)
        );

        jPanelConsignes.add(panelConsigne18);

        panelConsigne19.setPreferredSize(new java.awt.Dimension(35, 400));

        javax.swing.GroupLayout panelConsigne19Layout = new javax.swing.GroupLayout(panelConsigne19);
        panelConsigne19.setLayout(panelConsigne19Layout);
        panelConsigne19Layout.setHorizontalGroup(
            panelConsigne19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 33, Short.MAX_VALUE)
        );
        panelConsigne19Layout.setVerticalGroup(
            panelConsigne19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 398, Short.MAX_VALUE)
        );

        jPanelConsignes.add(panelConsigne19);

        panelConsigne20.setPreferredSize(new java.awt.Dimension(35, 400));

        javax.swing.GroupLayout panelConsigne20Layout = new javax.swing.GroupLayout(panelConsigne20);
        panelConsigne20.setLayout(panelConsigne20Layout);
        panelConsigne20Layout.setHorizontalGroup(
            panelConsigne20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 33, Short.MAX_VALUE)
        );
        panelConsigne20Layout.setVerticalGroup(
            panelConsigne20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 398, Short.MAX_VALUE)
        );

        jPanelConsignes.add(panelConsigne20);

        panelConsigne21.setPreferredSize(new java.awt.Dimension(35, 400));

        javax.swing.GroupLayout panelConsigne21Layout = new javax.swing.GroupLayout(panelConsigne21);
        panelConsigne21.setLayout(panelConsigne21Layout);
        panelConsigne21Layout.setHorizontalGroup(
            panelConsigne21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 33, Short.MAX_VALUE)
        );
        panelConsigne21Layout.setVerticalGroup(
            panelConsigne21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 398, Short.MAX_VALUE)
        );

        jPanelConsignes.add(panelConsigne21);

        panelConsigne22.setPreferredSize(new java.awt.Dimension(35, 400));

        javax.swing.GroupLayout panelConsigne22Layout = new javax.swing.GroupLayout(panelConsigne22);
        panelConsigne22.setLayout(panelConsigne22Layout);
        panelConsigne22Layout.setHorizontalGroup(
            panelConsigne22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 33, Short.MAX_VALUE)
        );
        panelConsigne22Layout.setVerticalGroup(
            panelConsigne22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 398, Short.MAX_VALUE)
        );

        jPanelConsignes.add(panelConsigne22);

        panelConsigne23.setPreferredSize(new java.awt.Dimension(35, 400));

        javax.swing.GroupLayout panelConsigne23Layout = new javax.swing.GroupLayout(panelConsigne23);
        panelConsigne23.setLayout(panelConsigne23Layout);
        panelConsigne23Layout.setHorizontalGroup(
            panelConsigne23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 33, Short.MAX_VALUE)
        );
        panelConsigne23Layout.setVerticalGroup(
            panelConsigne23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 398, Short.MAX_VALUE)
        );

        jPanelConsignes.add(panelConsigne23);

        panelConsigne24.setPreferredSize(new java.awt.Dimension(35, 400));

        javax.swing.GroupLayout panelConsigne24Layout = new javax.swing.GroupLayout(panelConsigne24);
        panelConsigne24.setLayout(panelConsigne24Layout);
        panelConsigne24Layout.setHorizontalGroup(
            panelConsigne24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 33, Short.MAX_VALUE)
        );
        panelConsigne24Layout.setVerticalGroup(
            panelConsigne24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 398, Short.MAX_VALUE)
        );

        jPanelConsignes.add(panelConsigne24);

        jButtonON.setBackground(java.awt.Color.green);
        jButtonON.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jButtonON.setText("ON");
        jButtonON.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonONActionPerformed(evt);
            }
        });

        jButtonOFF.setBackground(java.awt.Color.lightGray);
        jButtonOFF.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jButtonOFF.setText("OFF");
        jButtonOFF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOFFActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jButtonON, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jButtonOFF, javax.swing.GroupLayout.DEFAULT_SIZE, 123, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jButtonON, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(36, 36, 36)
                .addComponent(jButtonOFF, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 48, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 517, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 845, Short.MAX_VALUE)
                        .addComponent(jPanelConsignes, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jComboBox1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanelConsignes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );

        jTabbedPane1.addTab("ACCUEIL", jPanel1);

        jPanel2.setLayout(null);

        txt_service.setColumns(20);
        txt_service.setRows(5);
        jScrollPane2.setViewportView(txt_service);

        jPanel2.add(jScrollPane2);
        jScrollPane2.setBounds(100, 10, 910, 530);

        NOScroll.setText("NO Scroll");
        jPanel2.add(NOScroll);
        NOScroll.setBounds(0, 90, 100, 50);

        jButton1.setText("Clear");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel2.add(jButton1);
        jButton1.setBounds(0, 10, 100, 50);

        jTabbedPane1.addTab("SERVICE", jPanel2);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private int timerState = 1;
    private void jButtonOFFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOFFActionPerformed
        // STOP
        timerState = 0;
        EnvoiConsigne(0);
        jButtonON.setBackground(Color.lightGray);
        jButtonOFF.setBackground(Color.red);

    }//GEN-LAST:event_jButtonOFFActionPerformed

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
        // TODO add your handling code here:
        String newValue = (String) jComboBox1.getSelectedItem();
        SetConsignes(newValue);
        //repaint();
    }//GEN-LAST:event_jComboBox1ActionPerformed

    private void jButtonONActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonONActionPerformed
        // START
        EnvoiConsigne(1);
        timerState = 2;
        jButtonON.setBackground(Color.green);
        jButtonOFF.setBackground(Color.lightGray);
    }//GEN-LAST:event_jButtonONActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        txt_service.setText("");
    }//GEN-LAST:event_jButton1ActionPerformed

    // Consignes
    private int[] _consignes = null;
    private void SetConsignes(String consigne) {    
        if(consigne.equals("PRESENCE")) {
            _consignes[0]=18;
            _consignes[1]=18;
            _consignes[2]=18;
            _consignes[3]=18;
            _consignes[4]=18;
            _consignes[5]=18;
            _consignes[6]=20;
            _consignes[7]=22;
            _consignes[8]=22;
            _consignes[9]=22;
            _consignes[10]=22;
            _consignes[11]=22;
            _consignes[12]=22;
            _consignes[13]=22;
            _consignes[14]=22;
            _consignes[15]=22;
            _consignes[16]=22;
            _consignes[17]=22;
            _consignes[18]=22;
            _consignes[19]=22;
            _consignes[20]=22;
            _consignes[21]=21;
            _consignes[22]=20;
            _consignes[23]=19;
        }
        if(consigne.equals("TRAVAIL 1")) {
            _consignes[0]=18;
            _consignes[1]=18;
            _consignes[2]=18;
            _consignes[3]=18;
            _consignes[4]=18;
            _consignes[5]=18;
            _consignes[6]=20;
            _consignes[7]=22;
            _consignes[8]=22;
            _consignes[9]=20;
            _consignes[10]=18;
            _consignes[11]=18;
            _consignes[12]=18;
            _consignes[13]=18;
            _consignes[14]=18;
            _consignes[15]=18;
            _consignes[16]=20;
            _consignes[17]=22;
            _consignes[18]=22;
            _consignes[19]=22;
            _consignes[20]=22;
            _consignes[21]=21;
            _consignes[22]=20;
            _consignes[23]=19;
        }
        if(consigne.equals("TRAVAIL 2")) {
            _consignes[0]=18;
            _consignes[1]=18;
            _consignes[2]=18;
            _consignes[3]=18;
            _consignes[4]=18;
            _consignes[5]=18;
            _consignes[6]=20;
            _consignes[7]=22;
            _consignes[8]=22;
            _consignes[9]=20;
            _consignes[10]=18;
            _consignes[11]=20;
            _consignes[12]=22;
            _consignes[13]=20;
            _consignes[14]=18;
            _consignes[15]=18;
            _consignes[16]=20;
            _consignes[17]=22;
            _consignes[18]=22;
            _consignes[19]=22;
            _consignes[20]=22;
            _consignes[21]=21;
            _consignes[22]=20;
            _consignes[23]=19;
        }                
        if(consigne.equals("HORS GEL")) {
            for(int h=0; h<24; h++)
                _consignes[h] = 16;
        }
        if(consigne.equals("ZERO")) {
            for(int h=0; h<24; h++)
                _consignes[h] = 15;
        }
        if(consigne.equals("MAXIMUM")) {
            for(int h=0; h<24; h++)
                _consignes[h] = 25;
        }
        for(int h=0; h<24; h++) {
            PanelConsigne pc = (PanelConsigne) jPanelConsignes.getComponent(h);
            pc.setConsigne(_consignes[h]);
        }
    }    
    
    public void UpdateConsigneFromPanel(int h) {
         PanelConsigne pc = (PanelConsigne) jPanelConsignes.getComponent(h);
         _consignes[h] = pc.getConsigne();
    }
    // ***************************
    // **  SERIAL COMMUNICATION **
    // ***************************
    private SerialPort serialPort;
    private OutputStream outStream;
    private InputStream inStream;
    private BufferedReader input;
    
    public void connect(String portName) throws IOException, NoSuchPortException, PortInUseException, UnsupportedCommOperationException, TooManyListenersException {
        try {
            // Obtain a CommPortIdentifier object for the port you want to open
            CommPortIdentifier portId = CommPortIdentifier.getPortIdentifier(portName);
 
            // Get the port's ownership
            serialPort = (SerialPort) portId.open("GVB7", 5000);
 
            // Set the parameters of the connection.
            setSerialPortParameters();
 
            // Open the input and output streams for the connection. If they won't
            // open, close the port before throwing an exception.
            outStream = serialPort.getOutputStream();
            input = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
            inStream = serialPort.getInputStream();
            
            //serialPort.addEventListener(this);
            serialPort.notifyOnDataAvailable(true); // old true

        } catch (NoSuchPortException | PortInUseException e) {
            System.out.println("*** Exception dans connect (1ere ligne)");
            throw new IOException(e.getMessage());
        } catch (IOException e) {
            System.out.println("*** Exception dans connect (2ere ligne)");
            serialPort.close();
            throw e;
        }
    }
 
    /**
     * Get the serial port input stream
     * @return The serial port input stream
     */
    public InputStream getSerialInputStream() {
        return inStream;
    }
 
    /**
     * Get the serial port output stream
     * @return The serial port output stream
     */
    public OutputStream getSerialOutputStream() {
        return outStream;
    }
 
    /**
     * Sets the serial port parameters
     */
    private void setSerialPortParameters() throws IOException, UnsupportedCommOperationException {
        int baudRate = 9600; //57600; // 57600bps
 
        try {
            // Set serial port to 57600bps-8N1..my favourite
            serialPort.setSerialPortParams(
                    baudRate,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);
 
            serialPort.setFlowControlMode(
                    SerialPort.FLOWCONTROL_NONE);
        } catch (UnsupportedCommOperationException ex) {
            System.out.println("*** Exception dans setSerialPortParameters)");
            throw new IOException("Unsupported serial port parameter");
        }
    }    

    /*@Override
    public synchronized void serialEvent(SerialPortEvent spe) {
   
        //if (spe.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            try {
                    String inputLine = input.readLine();
                    if(inputLine != null && inputLine.length() > 0) {
                        txt_service.append( inputLine + "\n" );
                        if(jToggleREC.isSelected() == false && !(txt_service.getText().length() < 4096) ) {
                            txt_service.setText(txt_service.getText(1023, 1024));
                        }
                    }
                    //System.out.println(inputLine);
            } catch (Exception e) {
                    System.out.println("*** Exception dans serialEvent)");
                    System.err.println(e.toString());
            }
        //}
	// Ignore all the other eventTypes, but you should consider the other one
    }*/
    
        
    
    
    
    
    
    
    
    
    
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(NewJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(NewJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(NewJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(NewJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new NewJFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton NOScroll;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButtonOFF;
    private javax.swing.JButton jButtonON;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanelConsignes;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable jTable1;
    private chaudieregvb7_swing.PanelConsigne panelConsigne1;
    private chaudieregvb7_swing.PanelConsigne panelConsigne10;
    private chaudieregvb7_swing.PanelConsigne panelConsigne11;
    private chaudieregvb7_swing.PanelConsigne panelConsigne12;
    private chaudieregvb7_swing.PanelConsigne panelConsigne13;
    private chaudieregvb7_swing.PanelConsigne panelConsigne14;
    private chaudieregvb7_swing.PanelConsigne panelConsigne15;
    private chaudieregvb7_swing.PanelConsigne panelConsigne16;
    private chaudieregvb7_swing.PanelConsigne panelConsigne17;
    private chaudieregvb7_swing.PanelConsigne panelConsigne18;
    private chaudieregvb7_swing.PanelConsigne panelConsigne19;
    private chaudieregvb7_swing.PanelConsigne panelConsigne2;
    private chaudieregvb7_swing.PanelConsigne panelConsigne20;
    private chaudieregvb7_swing.PanelConsigne panelConsigne21;
    private chaudieregvb7_swing.PanelConsigne panelConsigne22;
    private chaudieregvb7_swing.PanelConsigne panelConsigne23;
    private chaudieregvb7_swing.PanelConsigne panelConsigne24;
    private chaudieregvb7_swing.PanelConsigne panelConsigne3;
    private chaudieregvb7_swing.PanelConsigne panelConsigne4;
    private chaudieregvb7_swing.PanelConsigne panelConsigne5;
    private chaudieregvb7_swing.PanelConsigne panelConsigne6;
    private chaudieregvb7_swing.PanelConsigne panelConsigne7;
    private chaudieregvb7_swing.PanelConsigne panelConsigne8;
    private chaudieregvb7_swing.PanelConsigne panelConsigne9;
    private javax.swing.JTextArea txt_service;
    // End of variables declaration//GEN-END:variables
}
