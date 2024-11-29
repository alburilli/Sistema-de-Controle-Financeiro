package br.com.ControleFinanceiro.telas;

import br.com.ControleFinanceiro.dal.ModuloConexao;
import java.io.*;
import java.sql.Connection;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author Anderson L
 */

public class MenCapTc6 extends javax.swing.JInternalFrame {

    private TelaPrincipal telaPrincipal;  // Campo para armazenar a instância de TelaPrincipal
    Connection conexao = null;

    public MenCapTc6(TelaPrincipal telaPrincipal) {
        initComponents();
        conexao = ModuloConexao.conector();
        this.telaPrincipal = telaPrincipal;  // Atribui a instância de TelaPrincipal ao campo
    }


private void realizarBackup() {
    JFileChooser fc = new JFileChooser();
    fc.setDialogTitle("Escolha o local para salvar o backup");
    fc.setFileFilter(new FileNameExtensionFilter("Arquivo SQL (*.sql)", "sql"));
    int userSelection = fc.showSaveDialog(this);

    if (userSelection == JFileChooser.APPROVE_OPTION) {
        File backupFile = fc.getSelectedFile();
        String path = backupFile.getAbsolutePath();
        if (!path.endsWith(".sql")) {
            path += ".sql";
        }

        String comando = String.format("\"C:\\xampp\\mysql\\bin\\mysqldump\" -u root dbfinanceiro > \"%s\"", path);

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                JOptionPane.showMessageDialog(null, "Backup em andamento, por favor aguarde...");
                try {
                    Process process = Runtime.getRuntime().exec(new String[]{"cmd.exe", "/c", comando});
                    int exitCode = process.waitFor();

                    if (exitCode == 0) {
                        JOptionPane.showMessageDialog(null, "Backup realizado com sucesso!");
                        // Atualiza os gráficos e tabelas na TelaPrincipal
                        SwingUtilities.invokeLater(() -> {
                            telaPrincipal.atualizarTabelaVencimentos();
                            telaPrincipal.criarGraficoPizza3D();
                        });
                    } else {
                        JOptionPane.showMessageDialog(null, "Erro ao realizar backup. Código de saída: " + exitCode);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "Erro ao realizar backup: " + e.getMessage());
                }
                return null;
            }
        }.execute();
    }
}

private void restaurarBackup() {
    JFileChooser fc = new JFileChooser();
    fc.setDialogTitle("Escolha o arquivo de backup para restaurar");
    fc.setFileFilter(new FileNameExtensionFilter("Arquivo SQL (*.sql)", "sql"));
    int userSelection = fc.showOpenDialog(this);

    if (userSelection == JFileChooser.APPROVE_OPTION) {
        File backupFile = fc.getSelectedFile();

        int confirma = JOptionPane.showConfirmDialog(null, 
            "Tem certeza que deseja restaurar o backup? Isso irá sobrescrever os dados atuais.", 
            "Atenção", JOptionPane.YES_NO_OPTION);

        if (confirma == JOptionPane.YES_OPTION) {
            String comando = "\"C:\\xampp\\mysql\\bin\\mysql\" -u root dbfinanceiro";

            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() {
                    JOptionPane.showMessageDialog(null, "Restauração em andamento, por favor aguarde...");
                    try {
                        Process process = Runtime.getRuntime().exec(new String[]{"cmd.exe", "/c", comando});

                        try (OutputStreamWriter writer = new OutputStreamWriter(process.getOutputStream());
                             BufferedReader reader = new BufferedReader(new FileReader(backupFile))) {

                            String line;
                            while ((line = reader.readLine()) != null) {
                                writer.write(line + System.lineSeparator());
                            }
                            writer.flush();
                        }

                        int exitCode = process.waitFor();
                        if (exitCode == 0) {
                            JOptionPane.showMessageDialog(null, "Restauração realizada com sucesso!");

                            // Atualiza os gráficos e tabelas na TelaPrincipal
                            SwingUtilities.invokeLater(() -> {
                                telaPrincipal.atualizarTabelaVencimentos();
                                telaPrincipal.criarGraficoPizza3D();
                            });
                        } else {
                            StringBuilder errorMsg = new StringBuilder();
                            try (BufferedReader errorReader = new BufferedReader(
                                    new InputStreamReader(process.getErrorStream()))) {
                                String errorLine;
                                while ((errorLine = errorReader.readLine()) != null) {
                                    errorMsg.append(errorLine).append(System.lineSeparator());
                                }
                            }
                            JOptionPane.showMessageDialog(null, "Erro ao restaurar backup: " + errorMsg.toString());
                        }
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(null, "Erro ao restaurar backup: " + e.getMessage());
                    }
                    return null;
                }
            }.execute();
        }
    }
}




    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btnBackup = new javax.swing.JButton();
        btnRestaurar = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setTitle("Tela de backup e recuperação do banco de dados");

        btnBackup.setText("Backup");
        btnBackup.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBackupActionPerformed(evt);
            }
        });

        btnRestaurar.setText("Restaurar");
        btnRestaurar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRestaurarActionPerformed(evt);
            }
        });

        jLabel1.setText("Backup e recuperação do banco de dados");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnBackup)
                        .addGap(61, 61, 61)
                        .addComponent(btnRestaurar))
                    .addComponent(jLabel1))
                .addContainerGap(237, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addComponent(jLabel1)
                .addGap(26, 26, 26)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnBackup)
                    .addComponent(btnRestaurar))
                .addContainerGap(232, Short.MAX_VALUE))
        );

        setBounds(0, 0, 472, 353);
    }// </editor-fold>//GEN-END:initComponents

    private void btnBackupActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBackupActionPerformed
       // Botão para realizar backup
       realizarBackup();
       
    }//GEN-LAST:event_btnBackupActionPerformed

    private void btnRestaurarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRestaurarActionPerformed
        // Botão para restaurar backup
        restaurarBackup();
    }//GEN-LAST:event_btnRestaurarActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBackup;
    private javax.swing.JButton btnRestaurar;
    private javax.swing.JLabel jLabel1;
    // End of variables declaration//GEN-END:variables
}
