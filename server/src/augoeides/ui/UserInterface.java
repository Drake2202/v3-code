/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package augoeides.ui;

import augoeides.world.World;
import it.gotoandplay.smartfoxserver.SmartFoxServer;
import it.gotoandplay.smartfoxserver.config.ConfigData;
import it.gotoandplay.smartfoxserver.data.Zone;
import it.gotoandplay.smartfoxserver.extensions.ExtensionHelper;
import it.gotoandplay.smartfoxserver.extensions.ExtensionManager;
import java.awt.Image;
import java.awt.event.ItemEvent;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.Timer;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author Mystical
 */
public class UserInterface extends javax.swing.JFrame {

    private static final long serialVersionUID = 1L;

    private World world;
    private Timer refreshTimer;

    /**
     * Creates new form UserInterface
     */
    public UserInterface(World world) throws IOException {
        initComponents();
        setLocationRelativeTo(null);
        Image i = ImageIO.read(getClass().getResource("/augoeides/ui/icon.ico"));
        setIconImage(i);

        this.world = world;
        this.refreshTimer = new Timer(1000, new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshTimerActionPerformed(evt);
            }
        });
        this.refreshTimer.setRepeats(true);

        refresh();
        this.chkAuto.setSelected(true);
    }

    private void refreshTimerActionPerformed(java.awt.event.ActionEvent evt) {
        refresh();
    }

    private String getUptime() {
        StringBuilder result = new StringBuilder();

        long now = System.currentTimeMillis();
        long start = SmartFoxServer.getInstance().getServerStartTime();

        long elapsed = now - start;
        int days = (int) Math.floor(elapsed / 86400000L);

        long temp = 86400000L * days;

        elapsed -= temp;
        int hours = (int) Math.floor(elapsed / 3600000L);

        temp = 3600000 * hours;
        elapsed -= temp;
        int minutes = (int) Math.floor(elapsed / 60000L);

        String s_days = String.valueOf(days);
        for (int i = 0; i < 4 - s_days.length(); i++)
            result.append("0");
        result.append(s_days);
        result.append(":");
        if (hours < 10)
            result.append("0");
        result.append(hours);
        result.append(":");
        if (minutes < 10)
            result.append("0");
        result.append(minutes);

        return result.toString();
    }

    private void refresh() {
        if (this.world == null || this.world.db == null) return;
        this.upTime.setText(getUptime());
        this.users.setText(String.valueOf(SmartFoxServer.getInstance().getGlobalUserCount()));
        this.rooms.setText(String.valueOf(SmartFoxServer.getInstance().getRoomNumber()));
        this.highestUserCount.setText(String.valueOf(ConfigData.maxSimultanousConnections));
        this.socketsConnected.setText(String.valueOf(SmartFoxServer.getInstance().getChannels().size()));
        this.activeThreads.setText(String.valueOf(Thread.activeCount()));
        this.numOfRestarts.setText(String.valueOf(ConfigData.restartCount));

        this.dbConnections.setText(String.valueOf(this.world.db.getActiveConnections()));

        this.dataOut.setText(FileUtils.byteCountToDisplaySize(ConfigData.dataOUT));
        this.dataIn.setText(FileUtils.byteCountToDisplaySize(ConfigData.dataIN));
        this.dataTotal.setText(FileUtils.byteCountToDisplaySize(ConfigData.dataIN + ConfigData.dataOUT));

        Runtime rt = Runtime.getRuntime();

        this.memoryUsed.setText(FileUtils.byteCountToDisplaySize(rt.totalMemory() - rt.freeMemory()));
        this.memoryTotal.setText(FileUtils.byteCountToDisplaySize(rt.totalMemory()));
        this.memoryFree.setText(FileUtils.byteCountToDisplaySize(rt.freeMemory()));

        this.memoryProgress.setMaximum(Long.valueOf(rt.totalMemory()).intValue());
        this.memoryProgress.setValue(Long.valueOf((rt.totalMemory() - rt.freeMemory())).intValue());

        int percentage = (int) ((rt.totalMemory() - rt.freeMemory()) * 100.0 / rt.totalMemory() + 0.5);
        this.memoryPercent.setText(percentage + "%");

        this.itemCount.setText(String.valueOf(this.world.items.size()));
        this.skillsCount.setText(String.valueOf(this.world.skills.size()));
        this.mapsCount.setText(String.valueOf(this.world.areas.size()));
        this.shopsCount.setText(String.valueOf(this.world.shops.size()));
        this.aurasCount.setText(String.valueOf(this.world.auras.size()));
        this.monstersCount.setText(String.valueOf(this.world.monsters.size()));
        this.questsCount.setText(String.valueOf(this.world.quests.size()));
        this.factionsCount.setText(String.valueOf(this.world.factions.size()));
        this.enhancementsCount.setText(String.valueOf(this.world.enhancements.size()));
        this.hairshopsCount.setText(String.valueOf(this.world.hairshops.size()));
        this.hairsCount.setText(String.valueOf(this.world.hairs.size()));
        this.effectsCount.setText(String.valueOf(this.world.effects.size()));
        this.partyCount.setText(String.valueOf(this.world.parties.size()));

        this.serverRates.setText(String.format("Server Rates: %dx EXP, %dx Gold, %dx Rep, %dx CP", this.world.EXP_RATE, this.world.GOLD_RATE, this.world.REP_RATE, this.world.CP_RATE));
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainTabPane = new javax.swing.JTabbedPane();
        panelStatus = new javax.swing.JPanel();
        btnRefresh = new javax.swing.JButton();
        chkAuto = new javax.swing.JCheckBox();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        numOfRestarts = new javax.swing.JLabel();
        activeThreads = new javax.swing.JLabel();
        socketsConnected = new javax.swing.JLabel();
        highestUserCount = new javax.swing.JLabel();
        users = new javax.swing.JLabel();
        rooms = new javax.swing.JLabel();
        upTime = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel27 = new javax.swing.JLabel();
        partyCount = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel18 = new javax.swing.JLabel();
        dbConnections = new javax.swing.JLabel();
        jLabel32 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel19 = new javax.swing.JLabel();
        dataIn = new javax.swing.JLabel();
        jLabel33 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        dataOut = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        dataTotal = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        memoryProgress = new javax.swing.JProgressBar();
        memoryPercent = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        memoryTotal = new javax.swing.JLabel();
        memoryUsed = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        memoryFree = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        itemCount = new javax.swing.JLabel();
        effectsCount = new javax.swing.JLabel();
        skillsCount = new javax.swing.JLabel();
        mapsCount = new javax.swing.JLabel();
        hairsCount = new javax.swing.JLabel();
        shopsCount = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        jLabel24 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        jLabel29 = new javax.swing.JLabel();
        questsCount = new javax.swing.JLabel();
        enhancementsCount = new javax.swing.JLabel();
        monstersCount = new javax.swing.JLabel();
        hairshopsCount = new javax.swing.JLabel();
        aurasCount = new javax.swing.JLabel();
        jLabel44 = new javax.swing.JLabel();
        factionsCount = new javax.swing.JLabel();
        btnRestart = new javax.swing.JButton();
        btnClear = new javax.swing.JButton();
        btnShutdown = new javax.swing.JButton();
        btnAbout = new javax.swing.JButton();
        serverRates = new javax.swing.JLabel();
        btnReload = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("augoeides/ui/Bundle"); // NOI18N
        setTitle(bundle.getString("UserInterface.title")); // NOI18N
        setMaximumSize(getPreferredSize());
        setMinimumSize(getPreferredSize());
        setResizable(false);

        btnRefresh.setText(bundle.getString("UserInterface.btnRefresh.text")); // NOI18N
        btnRefresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRefreshActionPerformed(evt);
            }
        });

        chkAuto.setText(bundle.getString("UserInterface.chkAuto.text")); // NOI18N
        chkAuto.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                chkAutoItemStateChanged(evt);
            }
        });

        jPanel1.setMaximumSize(new java.awt.Dimension(250, 200));
        jPanel1.setMinimumSize(new java.awt.Dimension(250, 200));
        jPanel1.setPreferredSize(new java.awt.Dimension(250, 200));

        jLabel1.setText(bundle.getString("UserInterface.jLabel1.text")); // NOI18N

        jLabel2.setText(bundle.getString("UserInterface.jLabel2.text")); // NOI18N

        jLabel3.setText(bundle.getString("UserInterface.jLabel3.text")); // NOI18N

        jLabel4.setText(bundle.getString("UserInterface.jLabel4.text")); // NOI18N

        jLabel5.setText(bundle.getString("UserInterface.jLabel5.text")); // NOI18N

        jLabel6.setText(bundle.getString("UserInterface.jLabel6.text")); // NOI18N

        jLabel7.setText(bundle.getString("UserInterface.jLabel7.text")); // NOI18N

        numOfRestarts.setText(bundle.getString("UserInterface.numOfRestarts.text")); // NOI18N

        activeThreads.setText(bundle.getString("UserInterface.activeThreads.text")); // NOI18N

        socketsConnected.setText(bundle.getString("UserInterface.socketsConnected.text")); // NOI18N

        highestUserCount.setText(bundle.getString("UserInterface.highestUserCount.text")); // NOI18N

        users.setText(bundle.getString("UserInterface.users.text")); // NOI18N

        rooms.setText(bundle.getString("UserInterface.rooms.text")); // NOI18N

        upTime.setText(bundle.getString("UserInterface.upTime.text")); // NOI18N

        jLabel17.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel17.setText(bundle.getString("UserInterface.jLabel17.text")); // NOI18N

        jLabel27.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel27.setText(bundle.getString("UserInterface.jLabel27.text")); // NOI18N

        partyCount.setText(bundle.getString("UserInterface.partyCount.text")); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel5)
                                    .addComponent(jLabel6)
                                    .addComponent(jLabel3)
                                    .addComponent(jLabel4)
                                    .addComponent(jLabel1)
                                    .addComponent(jLabel2))
                                .addGap(1, 1, 1)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 126, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(numOfRestarts, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(rooms, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(upTime, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(highestUserCount, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(users, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(socketsConnected, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(activeThreads, javax.swing.GroupLayout.Alignment.TRAILING)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel17)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel27)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(partyCount)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel17)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(upTime))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(rooms))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(users))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(highestUserCount))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(socketsConnected))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(activeThreads))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(numOfRestarts))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel27)
                    .addComponent(partyCount))
                .addContainerGap(15, Short.MAX_VALUE))
        );

        jPanel2.setMaximumSize(new java.awt.Dimension(250, 200));
        jPanel2.setMinimumSize(new java.awt.Dimension(250, 200));
        jPanel2.setPreferredSize(new java.awt.Dimension(250, 100));

        jLabel18.setText(bundle.getString("UserInterface.jLabel18.text")); // NOI18N

        dbConnections.setText(bundle.getString("UserInterface.dbConnections.text")); // NOI18N

        jLabel32.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel32.setText(bundle.getString("UserInterface.jLabel32.text")); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel18)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 128, Short.MAX_VALUE)
                        .addComponent(dbConnections))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel32)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addComponent(jLabel32)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(dbConnections)
                    .addComponent(jLabel18))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel3.setMaximumSize(new java.awt.Dimension(250, 200));
        jPanel3.setMinimumSize(new java.awt.Dimension(250, 200));
        jPanel3.setPreferredSize(new java.awt.Dimension(250, 100));

        jLabel19.setText(bundle.getString("UserInterface.jLabel19.text")); // NOI18N

        dataIn.setText(bundle.getString("UserInterface.dataIn.text")); // NOI18N

        jLabel33.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel33.setText(bundle.getString("UserInterface.jLabel33.text")); // NOI18N

        jLabel20.setText(bundle.getString("UserInterface.jLabel20.text")); // NOI18N

        dataOut.setText(bundle.getString("UserInterface.dataOut.text")); // NOI18N

        jLabel21.setText(bundle.getString("UserInterface.jLabel21.text")); // NOI18N

        dataTotal.setText(bundle.getString("UserInterface.dataTotal.text")); // NOI18N

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel19)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(dataIn))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel33)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel20)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 176, Short.MAX_VALUE)
                        .addComponent(dataOut))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel21)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(dataTotal)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel33)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel19)
                    .addComponent(dataIn))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel20)
                    .addComponent(dataOut))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel21)
                    .addComponent(dataTotal))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel8.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel8.setText(bundle.getString("UserInterface.jLabel8.text")); // NOI18N

        memoryPercent.setLabelFor(memoryProgress);
        memoryPercent.setText(bundle.getString("UserInterface.memoryPercent.text")); // NOI18N

        jLabel10.setText(bundle.getString("UserInterface.jLabel10.text")); // NOI18N

        jLabel11.setText(bundle.getString("UserInterface.jLabel11.text")); // NOI18N

        memoryTotal.setText(bundle.getString("UserInterface.memoryTotal.text")); // NOI18N

        memoryUsed.setText(bundle.getString("UserInterface.memoryUsed.text")); // NOI18N

        jLabel12.setText(bundle.getString("UserInterface.jLabel12.text")); // NOI18N

        memoryFree.setText(bundle.getString("UserInterface.memoryFree.text")); // NOI18N

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel10)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(memoryTotal))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel11)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(memoryUsed))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(memoryProgress, javax.swing.GroupLayout.DEFAULT_SIZE, 147, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(memoryPercent))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel12)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(memoryFree)))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel8)
                    .addComponent(memoryProgress, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(memoryPercent))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel10)
                    .addComponent(memoryTotal))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel11)
                    .addComponent(memoryUsed))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(memoryFree))
                .addContainerGap(15, Short.MAX_VALUE))
        );

        jLabel9.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel9.setText(bundle.getString("UserInterface.jLabel9.text")); // NOI18N

        jLabel13.setText(bundle.getString("UserInterface.jLabel13.text")); // NOI18N

        jLabel14.setText(bundle.getString("UserInterface.jLabel14.text")); // NOI18N

        jLabel15.setText(bundle.getString("UserInterface.jLabel15.text")); // NOI18N

        jLabel16.setText(bundle.getString("UserInterface.jLabel16.text")); // NOI18N

        jLabel22.setText(bundle.getString("UserInterface.jLabel22.text")); // NOI18N

        jLabel23.setText(bundle.getString("UserInterface.jLabel23.text")); // NOI18N

        itemCount.setText(bundle.getString("UserInterface.itemCount.text")); // NOI18N

        effectsCount.setText(bundle.getString("UserInterface.effectsCount.text")); // NOI18N

        skillsCount.setText(bundle.getString("UserInterface.skillsCount.text")); // NOI18N

        mapsCount.setText(bundle.getString("UserInterface.mapsCount.text")); // NOI18N

        hairsCount.setText(bundle.getString("UserInterface.hairsCount.text")); // NOI18N

        shopsCount.setText(bundle.getString("UserInterface.shopsCount.text")); // NOI18N

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jLabel14)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(effectsCount))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jLabel9)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jLabel13)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(itemCount))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jLabel15)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(skillsCount))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jLabel16)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(mapsCount))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jLabel22)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(hairsCount))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jLabel23)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(shopsCount)))
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel9)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13)
                    .addComponent(itemCount))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14)
                    .addComponent(effectsCount))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel15)
                    .addComponent(skillsCount))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel16)
                    .addComponent(mapsCount))
                .addGap(7, 7, 7)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel22)
                    .addComponent(hairsCount))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel23)
                    .addComponent(shopsCount))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel24.setText(bundle.getString("UserInterface.jLabel24.text")); // NOI18N

        jLabel25.setText(bundle.getString("UserInterface.jLabel25.text")); // NOI18N

        jLabel26.setText(bundle.getString("UserInterface.jLabel26.text")); // NOI18N

        jLabel28.setText(bundle.getString("UserInterface.jLabel28.text")); // NOI18N

        jLabel29.setText(bundle.getString("UserInterface.jLabel29.text")); // NOI18N

        questsCount.setText(bundle.getString("UserInterface.questsCount.text")); // NOI18N

        enhancementsCount.setText(bundle.getString("UserInterface.enhancementsCount.text")); // NOI18N

        monstersCount.setText(bundle.getString("UserInterface.monstersCount.text")); // NOI18N

        hairshopsCount.setText(bundle.getString("UserInterface.hairshopsCount.text")); // NOI18N

        aurasCount.setText(bundle.getString("UserInterface.aurasCount.text")); // NOI18N

        jLabel44.setText(bundle.getString("UserInterface.jLabel44.text")); // NOI18N

        factionsCount.setText(bundle.getString("UserInterface.factionsCount.text")); // NOI18N

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addComponent(jLabel24)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(questsCount))
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addComponent(jLabel25)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(enhancementsCount))
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addComponent(jLabel26)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(monstersCount))
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addComponent(jLabel28)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(hairshopsCount))
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addComponent(jLabel29)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(aurasCount))
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addComponent(jLabel44)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(factionsCount)))
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGap(31, 31, 31)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel25)
                    .addComponent(enhancementsCount))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel26)
                    .addComponent(monstersCount))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel28)
                    .addComponent(hairshopsCount))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel29)
                    .addComponent(aurasCount))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel44)
                    .addComponent(factionsCount))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel24)
                    .addComponent(questsCount))
                .addContainerGap(12, Short.MAX_VALUE))
        );

        btnRestart.setText(bundle.getString("UserInterface.btnRestart.text")); // NOI18N
        btnRestart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRestartActionPerformed(evt);
            }
        });

        btnClear.setText(bundle.getString("UserInterface.btnClear.text")); // NOI18N
        btnClear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClearActionPerformed(evt);
            }
        });

        btnShutdown.setText(bundle.getString("UserInterface.btnShutdown.text")); // NOI18N
        btnShutdown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnShutdownActionPerformed(evt);
            }
        });

        btnAbout.setText(bundle.getString("UserInterface.btnAbout.text")); // NOI18N
        btnAbout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAboutActionPerformed(evt);
            }
        });

        serverRates.setText(bundle.getString("UserInterface.serverRates.text")); // NOI18N

        btnReload.setText(bundle.getString("UserInterface.btnReload.text")); // NOI18N
        btnReload.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnReloadActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelStatusLayout = new javax.swing.GroupLayout(panelStatus);
        panelStatus.setLayout(panelStatusLayout);
        panelStatusLayout.setHorizontalGroup(
            panelStatusLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelStatusLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelStatusLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelStatusLayout.createSequentialGroup()
                        .addGroup(panelStatusLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(panelStatusLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(panelStatusLayout.createSequentialGroup()
                                .addGroup(panelStatusLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(panelStatusLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                            .addGroup(panelStatusLayout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(chkAuto)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnRefresh))))
                    .addGroup(panelStatusLayout.createSequentialGroup()
                        .addComponent(btnShutdown)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnRestart, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnAbout, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnClear)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnReload)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(serverRates)))
                .addContainerGap())
        );
        panelStatusLayout.setVerticalGroup(
            panelStatusLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelStatusLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelStatusLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(panelStatusLayout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                    .addGroup(panelStatusLayout.createSequentialGroup()
                        .addGroup(panelStatusLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panelStatusLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelStatusLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnRefresh)
                    .addComponent(chkAuto))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(panelStatusLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnShutdown)
                    .addComponent(btnRestart)
                    .addComponent(btnAbout)
                    .addComponent(btnClear)
                    .addComponent(serverRates)
                    .addComponent(btnReload))
                .addContainerGap())
        );

        mainTabPane.addTab(bundle.getString("UserInterface.panelStatus.TabConstraints.tabTitle"), panelStatus); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainTabPane)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainTabPane)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnShutdownActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnShutdownActionPerformed
        int result = MessageBox.showConfirm("Continue shutdown operation? Players will rage.", "Confirmation", JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            this.world.send(new String[]{"logoutWarning", "", "60"}, world.zone.getChannelList());
            this.world.shutdown();
            try {
                Thread.sleep(TimeUnit.SECONDS.toMillis(2));
            } catch (InterruptedException ex) {
            }
            System.exit(0);
        }
    }//GEN-LAST:event_btnShutdownActionPerformed

    private void btnClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearActionPerformed
        if (this.world.retrieveDatabaseObject("all"))
            MessageBox.showMessage("Data Memory Cleared!", "Operation Successful");
    }//GEN-LAST:event_btnClearActionPerformed

    private void btnRestartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRestartActionPerformed
        int result = MessageBox.showConfirm("Continue restart operation? Players will rage.", "Confirmation", JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            this.world.send(new String[]{"logoutWarning", "", "60"}, world.zone.getChannelList());
            this.world.shutdown();
            try {
                Thread.sleep(TimeUnit.SECONDS.toMillis(2));
            } catch (InterruptedException ex) {
            }
            ExtensionHelper.instance().rebootServer();
        }
    }//GEN-LAST:event_btnRestartActionPerformed

    private void chkAutoItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_chkAutoItemStateChanged
        if (evt.getStateChange() == ItemEvent.DESELECTED)
            this.refreshTimer.stop();
        else if (evt.getStateChange() == ItemEvent.SELECTED)
            this.refreshTimer.start();
    }//GEN-LAST:event_chkAutoItemStateChanged

    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshActionPerformed
        refresh();
    }//GEN-LAST:event_btnRefreshActionPerformed

    private void btnAboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAboutActionPerformed
        MessageBox.showMessage(
                "AugoEidEs (MExt v3) by Mystical" + System.getProperty("line.separator")
                + "(c) 2013 InfinityArts", "About"
        );
    }//GEN-LAST:event_btnAboutActionPerformed

    private void btnReloadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReloadActionPerformed
        String zoneName = "zone_master";
        String extName = "zm";
        Zone zone = SmartFoxServer.getInstance().getZone(zoneName);
        if (zone != null) {
            ExtensionManager em = zone.getExtManager();
            if (em != null)
                em.reloadExtension(extName);
        }

    }//GEN-LAST:event_btnReloadActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel activeThreads;
    private javax.swing.JLabel aurasCount;
    private javax.swing.JButton btnAbout;
    private javax.swing.JButton btnClear;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JButton btnReload;
    private javax.swing.JButton btnRestart;
    private javax.swing.JButton btnShutdown;
    private javax.swing.JCheckBox chkAuto;
    private javax.swing.JLabel dataIn;
    private javax.swing.JLabel dataOut;
    private javax.swing.JLabel dataTotal;
    private javax.swing.JLabel dbConnections;
    private javax.swing.JLabel effectsCount;
    private javax.swing.JLabel enhancementsCount;
    private javax.swing.JLabel factionsCount;
    private javax.swing.JLabel hairsCount;
    private javax.swing.JLabel hairshopsCount;
    private javax.swing.JLabel highestUserCount;
    private javax.swing.JLabel itemCount;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel44;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JTabbedPane mainTabPane;
    private javax.swing.JLabel mapsCount;
    private javax.swing.JLabel memoryFree;
    private javax.swing.JLabel memoryPercent;
    private javax.swing.JProgressBar memoryProgress;
    private javax.swing.JLabel memoryTotal;
    private javax.swing.JLabel memoryUsed;
    private javax.swing.JLabel monstersCount;
    private javax.swing.JLabel numOfRestarts;
    private javax.swing.JPanel panelStatus;
    private javax.swing.JLabel partyCount;
    private javax.swing.JLabel questsCount;
    private javax.swing.JLabel rooms;
    private javax.swing.JLabel serverRates;
    private javax.swing.JLabel shopsCount;
    private javax.swing.JLabel skillsCount;
    private javax.swing.JLabel socketsConnected;
    private javax.swing.JLabel upTime;
    private javax.swing.JLabel users;
    // End of variables declaration//GEN-END:variables
}
