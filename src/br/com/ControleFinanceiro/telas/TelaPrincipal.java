package br.com.ControleFinanceiro.telas;

import javax.swing.*;
import br.com.ControleFinanceiro.dal.ModuloConexao;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import javax.swing.table.DefaultTableModel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.util.Rotation;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author Anderson L
 */

public class TelaPrincipal extends javax.swing.JFrame {

    Connection conexao = null;
    
    

    public TelaPrincipal() {
        initComponents();
        conexao = ModuloConexao.conector();

        // Atualiza a tabela de vencimentos ao iniciar a tela
        atualizarTabelaVencimentos();
        criarGraficoPizza3D();
        
        // Atualiza o relógio e a data automaticamente
        new Timer(1000, e -> atualizarRelogio()).start();
    }

    // Método para atualizar o relógio
    private void atualizarRelogio() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        lblRelogio.setText(sdf.format(new Date()));
    }
    

void criarGraficoPizza3D() {
    String sql = "SELECT tp.nome AS tipo, SUM(t.valor) AS total " +
                 "FROM transacoes t " +
                 "JOIN descricoes d ON t.descricao_id = d.id " +
                 "JOIN tipos tp ON d.tipo_id = tp.id " +
                 "JOIN status_transacoes st ON t.status_id = st.id " +
                 "WHERE st.descricao = 'Pago' " + // Filtra apenas transações pagas
                 "GROUP BY tp.nome";

    DefaultPieDataset dataset = new DefaultPieDataset();

    try (PreparedStatement pst = conexao.prepareStatement(sql);
         ResultSet rs = pst.executeQuery()) {

        double totalReceitas = 0;
        double totalDespesas = 0;

        while (rs.next()) {
            String tipo = rs.getString("tipo");
            double total = rs.getDouble("total");

            if (tipo.equalsIgnoreCase("Receita")) {
                totalReceitas += total;
            } else if (tipo.equalsIgnoreCase("Despesa")) {
                totalDespesas += total;
            }
        }

        dataset.setValue("Receita", totalReceitas);
        dataset.setValue("Despesa", totalDespesas);

        JFreeChart pieChart = ChartFactory.createPieChart3D(
                "Distribuição de Transações Pagas",
                dataset,
                true,
                true,
                false
        );

        pieChart.setBackgroundPaint(Color.WHITE); 
        pieChart.getTitle().setFont(new Font("Arial", Font.BOLD, 18));
        pieChart.getTitle().setPaint(new Color(30, 144, 255)); 

        PiePlot3D plot = (PiePlot3D) pieChart.getPlot();
        plot.setStartAngle(290);
        plot.setDirection(Rotation.CLOCKWISE);
        plot.setForegroundAlpha(0.85f);

        plot.setBackgroundPaint(Color.WHITE); 
        plot.setOutlineVisible(true);
        
        plot.setSectionPaint("Receita", new Color(76, 175, 80));
        plot.setSectionOutlinePaint("Receita", new Color(56, 142, 60));
        plot.setSectionPaint("Despesa", new Color(244, 67, 54));
        plot.setSectionOutlinePaint("Despesa", new Color(211, 47, 47));

        plot.setLabelFont(new Font("Arial", Font.BOLD, 12));
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator(
                "{0}: {1} ({2})", NumberFormat.getCurrencyInstance(), NumberFormat.getPercentInstance()));

        plot.setSectionOutlinesVisible(true);
        
        ChartPanel chartPanel = new ChartPanel(pieChart);
        chartPanel.setPreferredSize(new Dimension(jPanel1.getWidth(), jPanel1.getHeight()));
        jPanel1.setLayout(new BorderLayout());
        jPanel1.removeAll();
        jPanel1.add(chartPanel, BorderLayout.CENTER);

        chartPanel.setMouseWheelEnabled(true);

        jPanel1.revalidate();
        jPanel1.repaint();
        pack();

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(null, e);
    }
}

// Método para atualizar a tabela de contas a vencer
void atualizarTabelaVencimentos() {
    // SQL para selecionar transações com data de vencimento para o ano atual
    String sql = "SELECT d.descricao, d.data_vencimento " +
                 "FROM descricoes d " +
                 "WHERE d.data_vencimento IS NOT NULL AND YEAR(d.data_vencimento) = YEAR(CURDATE())";

    try (PreparedStatement pst = conexao.prepareStatement(sql);
         ResultSet rs = pst.executeQuery()) {

        // Criação do modelo da tabela com cabeçalhos
        DefaultTableModel model = new DefaultTableModel(new Object[][] {}, new String[]{"Descrição", "Data de Vencimento", "Dias Restantes"});
        tblCAvencer.setModel(model);

        // Formatação da data
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        // Preenchendo a tabela com os dados recuperados
        while (rs.next()) {
            String descricao = rs.getString("descricao");
            Date dataVencimento = rs.getDate("data_vencimento");
            int diasRestantes = calcularDiasRestantes(dataVencimento);

            // Adiciona a linha na tabela com os dados da transação
            model.addRow(new Object[]{descricao, sdf.format(dataVencimento), formatarDiasRestantes(diasRestantes)});
        }

        // Aplica o custom renderer para a tabela
        tblCAvencer.setDefaultRenderer(Object.class, new CustomTableCellRenderer());

    } catch (SQLException e) {
        // Exibindo erro caso ocorra algum problema no SQL
        JOptionPane.showMessageDialog(null, "Erro ao atualizar tabela de vencimentos: " + e.getMessage());
    }
}

// Método para calcular os dias restantes até a data de vencimento
private int calcularDiasRestantes(Date dataVencimento) {
    Calendar calVencimento = Calendar.getInstance();
    calVencimento.setTime(dataVencimento);
    Calendar calAtual = Calendar.getInstance();

    // Calcula a diferença em milissegundos
    long diffInMillis = calVencimento.getTimeInMillis() - calAtual.getTimeInMillis();
    return (int) (diffInMillis / (1000 * 60 * 60 * 24));  // Retorna a diferença em dias
}

// Método para formatar os dias restantes para exibição na tabela
private String formatarDiasRestantes(int diasRestantes) {
    if (diasRestantes == 0) {
        return "Dia do Pagamento";  // Exibe quando é o dia do pagamento
    } else if (diasRestantes < 0) {
        return "Vencido";  // Exibe quando já passou da data
    } else {
        return String.valueOf(diasRestantes + 1);  // Exibe os dias restantes + 1 (próximo dia)
    }
}

// Custom renderer para a tabela, alterando as cores das linhas com base nos dias restantes
class CustomTableCellRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component cellComponent = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        // Obtém o valor de "Dias Restantes" na terceira coluna (índice 2)
        String diasRestantesTexto = (String) table.getValueAt(row, 2);
        int diasRestantes = 0;

        if (diasRestantesTexto.equals("Dia do Pagamento")) {
            diasRestantes = 0;
        } else if (diasRestantesTexto.equals("Vencido")) {
            diasRestantes = -1;
        } else {
            diasRestantes = Integer.parseInt(diasRestantesTexto);
        }

        // Define a cor de fundo da célula com base nos dias restantes
        if (diasRestantes < 0) {
            cellComponent.setBackground(new Color(255, 204, 204));  // "Vencido" - Vermelho suave
        } else if (diasRestantes == 0) {
            cellComponent.setBackground(new Color(204, 255, 204));  // "Dia do Pagamento" - Verde suave
        } else if (diasRestantes <= 3) {
            cellComponent.setBackground(new Color(255, 255, 204));  // Para 3 dias ou menos - Amarelo suave
        } else if (diasRestantes <= 10) {
            cellComponent.setBackground(new Color(255, 204, 153));  // Entre 4 e 10 dias - Laranja suave
        } else {
            cellComponent.setBackground(new Color(255, 255, 255));  // Mais de 10 dias - Branco
        }

        return cellComponent;
    }
}


    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        Desktop = new javax.swing.JDesktopPane();
        lblUsuario = new javax.swing.JLabel();
        lblRelogio = new javax.swing.JLabel();
        tabbedPaneCustom1 = new raven.tabbed.TabbedPaneCustom();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblCAvencer = new javax.swing.JTable();
        Menu = new javax.swing.JMenuBar();
        MenCadCat = new javax.swing.JMenu();
        MenCapTc1 = new javax.swing.JMenuItem();
        MenCapTc2 = new javax.swing.JMenuItem();
        MenCapTc3 = new javax.swing.JMenuItem();
        MenCapTc4 = new javax.swing.JMenuItem();
        MenCapTc5 = new javax.swing.JMenuItem();
        MenCapTc6 = new javax.swing.JMenuItem();
        MenAju = new javax.swing.JMenu();
        MenCapTcSobre = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Sistema para controle financeiro");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowActivated(java.awt.event.WindowEvent evt) {
                formWindowActivated(evt);
            }
        });

        Desktop.setPreferredSize(new java.awt.Dimension(841, 550));

        javax.swing.GroupLayout DesktopLayout = new javax.swing.GroupLayout(Desktop);
        Desktop.setLayout(DesktopLayout);
        DesktopLayout.setHorizontalGroup(
            DesktopLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1426, Short.MAX_VALUE)
        );
        DesktopLayout.setVerticalGroup(
            DesktopLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        lblUsuario.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        lblUsuario.setText("Usuário");

        lblRelogio.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        lblRelogio.setText("Timer:");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 405, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 324, Short.MAX_VALUE)
        );

        tabbedPaneCustom1.addTab("Grafico", jPanel1);

        tblCAvencer.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane2.setViewportView(tblCAvencer);

        tabbedPaneCustom1.addTab("Próximos do vencimento", jScrollPane2);

        MenCadCat.setText("Gerenciamento Financeiro");

        MenCapTc1.setText("Cadastro de Usuário e senha");
        MenCapTc1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MenCapTc1ActionPerformed(evt);
            }
        });
        MenCadCat.add(MenCapTc1);

        MenCapTc2.setText("Cadastro em relacionar os tipos nas descrições");
        MenCapTc2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MenCapTc2ActionPerformed(evt);
            }
        });
        MenCadCat.add(MenCapTc2);

        MenCapTc3.setText("Cadastrar as transações");
        MenCapTc3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MenCapTc3ActionPerformed(evt);
            }
        });
        MenCadCat.add(MenCapTc3);

        MenCapTc4.setText("Tela para visualizar o Controle Fluxo de Caixa");
        MenCapTc4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MenCapTc4ActionPerformed(evt);
            }
        });
        MenCadCat.add(MenCapTc4);

        MenCapTc5.setText("Graficos Anual / Tipos / Descrição / Mensal");
        MenCapTc5.setEnabled(false);
        MenCapTc5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MenCapTc5ActionPerformed(evt);
            }
        });
        MenCadCat.add(MenCapTc5);

        MenCapTc6.setText("Backup & Restore / Banco de dados");
        MenCapTc6.setEnabled(false);
        MenCapTc6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MenCapTc6ActionPerformed(evt);
            }
        });
        MenCadCat.add(MenCapTc6);

        Menu.add(MenCadCat);

        MenAju.setText("Ajuda");

        MenCapTcSobre.setText("Tela Sobre");
        MenCapTcSobre.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MenCapTcSobreActionPerformed(evt);
            }
        });
        MenAju.add(MenCapTcSobre);

        Menu.add(MenAju);

        setJMenuBar(Menu);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(Desktop, javax.swing.GroupLayout.PREFERRED_SIZE, 1426, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tabbedPaneCustom1, javax.swing.GroupLayout.DEFAULT_SIZE, 410, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblRelogio)
                            .addComponent(lblUsuario))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(32, 32, 32)
                .addComponent(lblUsuario)
                .addGap(32, 32, 32)
                .addComponent(lblRelogio)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 250, Short.MAX_VALUE)
                .addComponent(tabbedPaneCustom1, javax.swing.GroupLayout.PREFERRED_SIZE, 360, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(22, 22, 22))
            .addComponent(Desktop, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 747, Short.MAX_VALUE)
        );

        setSize(new java.awt.Dimension(1872, 807));
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowActivated(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowActivated
                                   
    }//GEN-LAST:event_formWindowActivated

    private void MenCapTc1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MenCapTc1ActionPerformed
        // TODO add your handling code here:
        MenCapTc1 mencadusuv = new MenCapTc1();
        mencadusuv.setVisible(true);
        Desktop.add(mencadusuv);
    }//GEN-LAST:event_MenCapTc1ActionPerformed

    private void MenCapTc2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MenCapTc2ActionPerformed
        // TODO add your handling code here:
        MenCapTc2 mencadp = new MenCapTc2(this);
        mencadp.setVisible(true);
        Desktop.add(mencadp);
    }//GEN-LAST:event_MenCapTc2ActionPerformed

    private void MenCapTc6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MenCapTc6ActionPerformed
        // TODO add your handling code here:
        MenCapTc6 mencaptc6 = new MenCapTc6(this);
        mencaptc6.setVisible(true);
        Desktop.add(mencaptc6);
    }//GEN-LAST:event_MenCapTc6ActionPerformed

    private void MenCapTc5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MenCapTc5ActionPerformed
        // TODO add your handling code here:
        MenCapTc5 mencaptc5 = new MenCapTc5();
        mencaptc5.setVisible(true);
        Desktop.add(mencaptc5);
    }//GEN-LAST:event_MenCapTc5ActionPerformed

    private void MenCapTc3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MenCapTc3ActionPerformed
        // TODO add your handling code here:
        MenCapTc3 mencaptc7 = new MenCapTc3(this);
        mencaptc7.setVisible(true);
        Desktop.add(mencaptc7);
    }//GEN-LAST:event_MenCapTc3ActionPerformed

    private void MenCapTc4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MenCapTc4ActionPerformed
        // TODO add your handling code here:
        MenCapTc4 mencaptc4 = new MenCapTc4();
        mencaptc4.setVisible(true);
        Desktop.add(mencaptc4);
    }//GEN-LAST:event_MenCapTc4ActionPerformed

    private void MenCapTcSobreActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MenCapTcSobreActionPerformed
        // TODO add your handling code here:
        MenCapTcSobre mencaptcsobre = new MenCapTcSobre();
        mencaptcsobre.setVisible(true);
        Desktop.add(mencaptcsobre);
    }//GEN-LAST:event_MenCapTcSobreActionPerformed

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
            java.util.logging.Logger.getLogger(TelaPrincipal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(TelaPrincipal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(TelaPrincipal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(TelaPrincipal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new TelaPrincipal().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JDesktopPane Desktop;
    private javax.swing.JMenu MenAju;
    private javax.swing.JMenu MenCadCat;
    private javax.swing.JMenuItem MenCapTc1;
    private javax.swing.JMenuItem MenCapTc2;
    private javax.swing.JMenuItem MenCapTc3;
    private javax.swing.JMenuItem MenCapTc4;
    public static javax.swing.JMenuItem MenCapTc5;
    public static javax.swing.JMenuItem MenCapTc6;
    private javax.swing.JMenuItem MenCapTcSobre;
    private javax.swing.JMenuBar Menu;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblRelogio;
    public static javax.swing.JLabel lblUsuario;
    private raven.tabbed.TabbedPaneCustom tabbedPaneCustom1;
    private javax.swing.JTable tblCAvencer;
    // End of variables declaration//GEN-END:variables
}
