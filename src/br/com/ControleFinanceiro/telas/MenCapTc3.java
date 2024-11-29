package br.com.ControleFinanceiro.telas;

import br.com.ControleFinanceiro.dal.ModuloConexao;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import javax.swing.JFormattedTextField;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.NumberFormatter;

/**
 *
 * @author Anderson L
 */


public class MenCapTc3 extends javax.swing.JInternalFrame {
private TelaPrincipal telaPrincipal;  // Campo para armazenar a instância de TelaPrincipal
    Connection conexao = null;
    PreparedStatement pst = null;
    ResultSet rs = null;
    

    

public MenCapTc3(TelaPrincipal telaPrincipal) {
    initComponents();
    
    this.telaPrincipal = telaPrincipal;  // Atribui a instância de TelaPrincipal ao campo
    
    // O restante da inicialização da classe
    conexao = ModuloConexao.conector();
    popularTipos();
    popularStatus();
    popularStatusComboBox();
    configurarEventoCmbTipos();
    atualizarTabelaTransacoes();
    

    
    
        // Adiciona o ActionListener uma vez
    jComboBox1.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent evt) {
            filtrarTabelaPorStatus(); // Chama o método para filtrar a tabela
        }
    });
    
    // Adiciona o ActionListener depois de inicializar jComboBox1
    //jComboBox1.addActionListener(evt -> filtrarTabelaPorStatus());
    jCheckBox1.addActionListener(evt -> jDateChooser1.setEnabled(jCheckBox1.isSelected()));
    jCheckBox2.addActionListener(evt -> jDateChooser3.setEnabled(jCheckBox2.isSelected()));
    
    // Adiciona o listener para popular a data de vencimento ao selecionar uma descrição
    cmbdescricoes.addActionListener(evt -> preencherDataVencimento());
}



private void filtrarTabelaPorStatus() {
    String statusSelecionado = (String) jComboBox1.getSelectedItem();
    String sql;

    // Verifica se um status específico foi selecionado
    if (statusSelecionado != null && !statusSelecionado.equals("Total Geral")) {
        // Consulta com o filtro de status selecionado
        sql = "SELECT t.id, tp.nome AS tipo, d.descricao, t.valor, d.data_vencimento, s.descricao AS status, t.proxima_leitura " +
              "FROM transacoes t " +
              "JOIN descricoes d ON t.descricao_id = d.id " +
              "JOIN tipos tp ON d.tipo_id = tp.id " +
              "LEFT JOIN status_transacoes s ON t.status_id = s.id " +  // LEFT JOIN para incluir transações sem status
              "WHERE s.descricao = ?";
    } else {
        // Consulta sem filtro de status, usa LEFT JOIN para trazer transações sem status
        sql = "SELECT t.id, tp.nome AS tipo, d.descricao, t.valor, d.data_vencimento, s.descricao AS status, t.proxima_leitura " +
              "FROM transacoes t " +
              "JOIN descricoes d ON t.descricao_id = d.id " +
              "JOIN tipos tp ON d.tipo_id = tp.id " +
              "LEFT JOIN status_transacoes s ON t.status_id = s.id";  // LEFT JOIN para incluir transações sem status
    }

    try {
        // Prepara a consulta
        pst = conexao.prepareStatement(sql);

        // Define o parâmetro de status, se necessário
        if (statusSelecionado != null && !statusSelecionado.equals("Total Geral")) {
            pst.setString(1, statusSelecionado);  // Define o valor de status para a consulta
        }

        // Executa a consulta
        rs = pst.executeQuery();
        DefaultTableModel modelo = (DefaultTableModel) tblTransacoes.getModel();
        modelo.setRowCount(0); // Limpa a tabela antes de atualizar

        // Preenche a tabela com os resultados
        while (rs.next()) {
            modelo.addRow(new Object[] {
                rs.getInt("id"),               // ID oculto
                rs.getString("tipo"),
                rs.getString("descricao"),
                rs.getDouble("valor"),
                rs.getDate("data_vencimento"),
                rs.getString("status"),
                rs.getDate("proxima_leitura")
            });
        }

        // Oculta a coluna do ID (primeira coluna)
        tblTransacoes.getColumnModel().getColumn(0).setMinWidth(0);
        tblTransacoes.getColumnModel().getColumn(0).setMaxWidth(0);
        tblTransacoes.getColumnModel().getColumn(0).setPreferredWidth(0);

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(null, "Erro ao filtrar a tabela: " + e.getMessage());
    } finally {
        // Fecha os recursos para evitar vazamento de memória
        try {
            if (rs != null) rs.close();
            if (pst != null) pst.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}



private void popularStatusComboBox() {
    String sql = "SELECT descricao FROM status_transacoes"; // Seleciona as descrições dos status
    try {
        pst = conexao.prepareStatement(sql);
        rs = pst.executeQuery();
        
        jComboBox1.addItem("Selecionar Status"); // Adiciona a opção "Selecionar Status"
        
        while (rs.next()) {
            jComboBox1.addItem(rs.getString("descricao"));
        }
        
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(null, "Erro ao carregar os status: " + e.getMessage());
    } finally {
        try {
            if (rs != null) rs.close();
            if (pst != null) pst.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}

private void popularDescricoes() {
        String tipoSelecionado = (String) cmbTipos.getSelectedItem();
        if (tipoSelecionado != null) {
            String sql = "SELECT d.id, d.descricao FROM descricoes d "
                       + "JOIN tipos t ON d.tipo_id = t.id WHERE t.nome = ?";
            try {
                pst = conexao.prepareStatement(sql);
                pst.setString(1, tipoSelecionado);
                rs = pst.executeQuery();
                cmbdescricoes.removeAllItems();
                while (rs.next()) {
                    cmbdescricoes.addItem(rs.getString("descricao"));
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e);
            }
        }
    }
    

  

private int getDescricaoId(String descricao) {
        int descricaoId = -1;
        String sql = "SELECT id FROM descricoes WHERE descricao = ?";
        
        try (PreparedStatement pst = conexao.prepareStatement(sql)) {
            pst.setString(1, descricao);
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                descricaoId = rs.getInt("id");
            }
        } catch (SQLException e) {
            System.err.println("Erro ao obter ID da descrição: " + e.getMessage());
            e.printStackTrace();
        }
        
        return descricaoId;
    }

private int getStatusId(String status) {
        int statusId = -1;
        String sql = "SELECT id FROM status_transacoes WHERE descricao = ?";
        
        try (PreparedStatement pst = conexao.prepareStatement(sql)) {
            pst.setString(1, status);
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                statusId = rs.getInt("id");
            }
        } catch (SQLException e) {
            System.err.println("Erro ao obter ID do status: " + e.getMessage());
            e.printStackTrace();
        }
        
        return statusId;
    }
    
private void popularStatus() {
        String sql = "SELECT descricao FROM status_transacoes";
        try {
            pst = conexao.prepareStatement(sql);
            rs = pst.executeQuery();
            cmbStatus.removeAllItems();
            
           cmbStatus.addItem("Selecionar Status"); // Adiciona a opção "Selecionar Status"
           
            while (rs.next()) {
                cmbStatus.addItem(rs.getString("descricao"));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }
 
private void configurarEventoCmbTipos() {
        cmbTipos.addActionListener(evt -> popularDescricoes());
    }

private void popularTipos() {
        String sql = "SELECT nome FROM tipos";
        try {
            pst = conexao.prepareStatement(sql);
            rs = pst.executeQuery();
            cmbTipos.removeAllItems();
            
            cmbTipos.addItem("Selecionar Status"); // Adiciona a opção "Selecionar Status"
            
            while (rs.next()) {
                cmbTipos.addItem(rs.getString("nome"));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }
    
    // Método para liberar ou bloquear jDateChooser1
private void liberarDataProximaLeitura() {
    jDateChooser1.setEnabled(jCheckBox1.isSelected());
}

    // Método para liberar ou bloquear jDateChooser1
private void liberarDataDoPagamento() {
    jDateChooser3.setEnabled(jCheckBox2.isSelected());
}
   
 
private void limparCampos() {
    cmbTipos.setSelectedIndex(-1);            // Limpa o campo de tipos
    cmbdescricoes.setSelectedIndex(-1);        // Limpa o campo de descrições
    txtValor.setText("");                      // Limpa o campo de valor
    jDateChooser1.setDate(null);               // Limpa a data da próxima leitura
    jDateChooser2.setDate(null);               // Limpa a data de pagamento
    jDateChooser3.setDate(null);               // Limpa a data de vencimento
    cmbStatus.setSelectedIndex(-1);            // Limpa o campo de status
    
    // Limpa o campo de data de vencimento e desmarca/desabilita o checkbox e o campo
    jCheckBox1.setSelected(false);             // Desmarca o checkbox
    jCheckBox2.setSelected(false);             // Desmarca o checkbox
    
    jDateChooser1.setEnabled(false);           // Desabilita o campo de data de vencimento
    jDateChooser3.setEnabled(false);           // Desabilita o campo de data de vencimento
}

private void atualizarTabelaTransacoes() {
    String sql = "SELECT t.id AS id, " +
                 "tp.nome AS tipo, " +
                 "d.descricao AS descricao, " +
                 "t.valor AS valor, " +
                 "t.data_vencimento AS data_vencimento, " + // Data de vencimento da transação
                 "s.descricao AS status, " +
                 "t.data_pagamento AS data_pagamento, " + // Data de pagamento
                 "t.proxima_leitura AS proxima_leitura " + // Próxima leitura
                 "FROM transacoes t " +
                 "JOIN descricoes d ON t.descricao_id = d.id " +
                 "JOIN tipos tp ON d.tipo_id = tp.id " +
                 "LEFT JOIN status_transacoes s ON t.status_id = s.id"; // LEFT JOIN para transações sem status

    try {
        pst = conexao.prepareStatement(sql);
        rs = pst.executeQuery();
        
        // Limpa e redefine a tabela
        DefaultTableModel modelo = (DefaultTableModel) tblTransacoes.getModel();
        modelo.setRowCount(0); // Limpa a tabela antes de atualizar

        while (rs.next()) {
            modelo.addRow(new Object[]{
                rs.getInt("id"),                      // ID da transação (oculto)
                rs.getString("tipo"),                 // Tipo
                rs.getString("descricao"),            // Descrição
                rs.getDouble("valor"),                // Valor
                rs.getDate("data_vencimento"),        // Data de Vencimento
                rs.getString("status"),               // Status
                rs.getDate("data_pagamento"),         // Data de Pagamento
                rs.getDate("proxima_leitura")         // Próxima Leitura
            });
        }

        // Oculta a coluna ID na tabela, mantendo a ordem das demais colunas
        tblTransacoes.getColumnModel().getColumn(0).setMinWidth(0);
        tblTransacoes.getColumnModel().getColumn(0).setMaxWidth(0);
        tblTransacoes.getColumnModel().getColumn(0).setPreferredWidth(0);

        // Configura o renderer para a coluna de status
        tblTransacoes.getColumnModel().getColumn(5).setCellRenderer(new StatusCellRenderer());

    } catch (Exception e) {
        JOptionPane.showMessageDialog(null, "Erro ao atualizar a tabela de transações: " + e.getMessage());
    } finally {
        // Fechar ResultSet e PreparedStatement se necessário
        try {
            if (rs != null) rs.close();
            if (pst != null) pst.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
 
     // Classe para renderizar a célula de status com cores específicas
public class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            String status = (String) value;
            if (status != null) {
                switch (status) {
                    case "Pago":
                        setBackground(new Color(144, 238, 144)); // Verde claro
                        setForeground(Color.BLACK);
                        break;
                    case "Pendente":
                        setBackground(new Color(255, 255, 224)); // Amarelo claro
                        setForeground(Color.BLACK);
                        break;
                    case "Cancelado":
                        setBackground(new Color(255, 99, 71)); // Vermelho
                        setForeground(Color.BLACK);
                        break;
                    default:
                        setBackground(Color.WHITE);
                        setForeground(Color.BLACK);
                        break;
                }
            }

            if (isSelected) {
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            }

            return this;
        }
    }

private int obterTipoId(String tipoNome) {
    String sql = "SELECT id FROM tipos WHERE nome = ?";
    try {
        pst = conexao.prepareStatement(sql);
        pst.setString(1, tipoNome);
        rs = pst.executeQuery();
        if (rs.next()) {
            return rs.getInt("id");
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Erro ao buscar tipo: " + e.getMessage());
    }
    return -1; // Retorna -1 se não encontrar o tipo
}

private int obterStatusId(String statusDescricao) {
    String sql = "SELECT id FROM status_transacoes WHERE descricao = ?";
    try {
        pst = conexao.prepareStatement(sql);
        pst.setString(1, statusDescricao);
        rs = pst.executeQuery();
        if (rs.next()) {
            return rs.getInt("id");
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Erro ao buscar status: " + e.getMessage());
    }
    return -1; // Retorna -1 se não encontrar o status
}

private void preencherDataVencimento() {
        String descricaoSelecionada = (String) cmbdescricoes.getSelectedItem();
        if (descricaoSelecionada != null) {
            int descricaoId = getDescricaoId(descricaoSelecionada);
            java.sql.Date dataVencimento = getDataVencimentoPorDescricao(descricaoId);
            jDateChooser3.setDate(dataVencimento);
            jCheckBox2.setSelected(false); // Inicialmente desmarcado, impedindo edição até o usuário selecionar
            jDateChooser3.setEnabled(false); // Desabilitado por padrão
        }
    }

private int obterDescricaoId(String descricao) {
    int descricaoId = -1;
    String sql = "SELECT id FROM descricoes WHERE descricao = ?";
    
    try (PreparedStatement pst = conexao.prepareStatement(sql)) {
        pst.setString(1, descricao);
        try (ResultSet rs = pst.executeQuery()) {
            if (rs.next()) {
                descricaoId = rs.getInt("id");
            }
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Erro ao obter o ID da descrição: " + e.getMessage(), "Erro de Banco de Dados", JOptionPane.ERROR_MESSAGE);
    }
    return descricaoId;
}

private java.sql.Date getDataVencimentoPorDescricao(int descricaoId) {
        String sql = "SELECT data_vencimento FROM descricoes WHERE id = ?";
        try (PreparedStatement pst = conexao.prepareStatement(sql)) {
            pst.setInt(1, descricaoId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getDate("data_vencimento");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erro ao buscar a data de vencimento: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace(); // Log para debug
        }
        return null; // Retorna null se não houver data de vencimento
    }
  
private java.sql.Date obterDataVencimentoOriginal(int descricaoId) {
    String sql = "SELECT data_vencimento FROM descricoes WHERE id = ?"; // Ajustado para 'id'
    PreparedStatement pst = null;
    ResultSet rs = null;
    java.sql.Date dataVencimento = null;

    try {
        pst = conexao.prepareStatement(sql);
        pst.setInt(1, descricaoId);  // Usa o id correto
        rs = pst.executeQuery();
        
        if (rs.next()) {
            dataVencimento = rs.getDate("data_vencimento");
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Erro ao obter a data original: " + e.getMessage(), "Erro de Banco de Dados", JOptionPane.ERROR_MESSAGE);
    } finally {
        try {
            if (rs != null) rs.close();
            if (pst != null) pst.close();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao fechar o PreparedStatement: " + ex.getMessage(), "Erro de Fechamento", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    return dataVencimento;
}

private boolean atualizarDataVencimento(int descricaoId, java.sql.Date novaDataVencimento) {
    String sql = "UPDATE descricoes SET data_vencimento = ? WHERE id = ?";

    // Usando try-with-resources para garantir que o PreparedStatement seja fechado automaticamente
    try (PreparedStatement pst = conexao.prepareStatement(sql)) {
        
        // Definindo os parâmetros da consulta
        pst.setDate(1, novaDataVencimento);
        pst.setInt(2, descricaoId); // ID da descrição
        
        // Executando a atualização e verificando se a atualização foi bem-sucedida
        return pst.executeUpdate() > 0; // Retorna true se a atualização foi bem-sucedida
        
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, 
            "Erro ao atualizar a data de vencimento: " + e.getMessage() + 
            "\nCódigo de erro: " + e.getErrorCode(), 
            "Erro de Banco de Dados", 
            JOptionPane.ERROR_MESSAGE
        );
        return false;
    }
}

private boolean adicionarTransacao(java.awt.event.ActionEvent evt) {
    // Verificar se os campos obrigatórios estão preenchidos
    if (cmbTipos.getSelectedIndex() == -1 || cmbdescricoes.getSelectedIndex() == -1 || txtValor.getText().isEmpty() || cmbStatus.getSelectedIndex() == -1) {
        JOptionPane.showMessageDialog(this, "Por favor, preencha todos os campos obrigatórios.", "Erro de Preenchimento", JOptionPane.ERROR_MESSAGE);
        return false;
    }

    // Capturar os dados do formulário
    String tipoSelecionado = (String) cmbTipos.getSelectedItem();
    String descricaoSelecionada = (String) cmbdescricoes.getSelectedItem();
    
    // Substituir vírgula por ponto no valor
    String valorString = txtValor.getText().replace(",", ".");
    
    // Verificar se o valor é válido (double)
    double valor;
    try {
        valor = Double.parseDouble(valorString);
    } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(this, "Valor inválido. Certifique-se de usar o formato correto (ex: 50.00).", "Erro no Valor", JOptionPane.ERROR_MESSAGE);
        return false;
    }

    // Definir as datas (podem ser nulas)
    java.sql.Date dataLeitura = (jDateChooser1.getDate() != null) ? new java.sql.Date(jDateChooser1.getDate().getTime()) : null;
    java.sql.Date dataPagamento = (jDateChooser2.getDate() != null) ? new java.sql.Date(jDateChooser2.getDate().getTime()) : null;
    java.sql.Date dataVencimento = (jDateChooser3.getDate() != null) ? new java.sql.Date(jDateChooser3.getDate().getTime()) : null;

    String statusSelecionado = (String) cmbStatus.getSelectedItem();

    // Obter os IDs dos itens selecionados
    int tipoId = obterTipoId(tipoSelecionado);
    int descricaoId = obterDescricaoId(descricaoSelecionada);
    int statusId = obterStatusId(statusSelecionado);

    // Verificar se os IDs são válidos
    if (tipoId == -1 || descricaoId == -1 || statusId == -1) {
        JOptionPane.showMessageDialog(this, "Erro ao obter as informações selecionadas. Por favor, tente novamente.", "Erro de Seleção", JOptionPane.ERROR_MESSAGE);
        return false;
    }

    // Verificar se a data de vencimento precisa ser atualizada na tabela `descricoes`
    java.sql.Date dataVencimentoAtual = obterDataVencimentoOriginal(descricaoId);
    if (dataVencimento != null && !dataVencimento.equals(dataVencimentoAtual)) {
        int resposta = JOptionPane.showConfirmDialog(this, "A data de vencimento foi alterada. Deseja atualizar a data de vencimento na tabela 'descricoes'?", "Alteração de Data", JOptionPane.YES_NO_OPTION);
        if (resposta == JOptionPane.YES_OPTION) {
            if (!atualizarDataVencimento(descricaoId, dataVencimento)) {
                JOptionPane.showMessageDialog(this, "Erro ao atualizar a data de vencimento na tabela 'descricoes'.", "Erro de Banco de Dados", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } else {
            dataVencimento = dataVencimentoAtual; // Manter a data original
        }
    }

    // Inserir a transação no banco de dados
    String sql = "INSERT INTO transacoes (descricao_id, valor, data_pagamento, proxima_leitura, data_vencimento, status_id) VALUES (?, ?, ?, ?, ?, ?)";
    
    try (PreparedStatement pst = conexao.prepareStatement(sql)) {
        pst.setInt(1, descricaoId);
        pst.setDouble(2, valor);
        pst.setDate(3, dataPagamento);
        pst.setDate(4, dataLeitura);
        pst.setDate(5, dataVencimento);
        pst.setInt(6, statusId);

        // Executar a inserção
        pst.executeUpdate();
        JOptionPane.showMessageDialog(this, "Transação adicionada com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);

        // Atualizar a tabela de transações após a inserção
        atualizarTabelaTransacoes();

        telaPrincipal.atualizarTabelaVencimentos();
        telaPrincipal.criarGraficoPizza3D();

        // Limpar os campos após adicionar
        limparCampos();
        
        return true; // Retorna true se a transação foi adicionada com sucesso

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Erro ao adicionar a transação no banco de dados: " + e.getMessage(), "Erro de Banco de Dados", JOptionPane.ERROR_MESSAGE);
        e.printStackTrace(); // Exibir o stack trace no console para depuração
        return false; // Retorna false se ocorrer um erro
    }
}

private void excluirTransacao(java.awt.event.ActionEvent evt) {
    // Verificar se uma transação foi selecionada na tabela
    int linhaSelecionada = tblTransacoes.getSelectedRow();
    if (linhaSelecionada == -1) {
        JOptionPane.showMessageDialog(this, "Por favor, selecione uma transação para excluir.", "Erro de Seleção", JOptionPane.ERROR_MESSAGE);
        return;
    }

    // Obter o ID da transação selecionada
    int idTransacao = (int) tblTransacoes.getValueAt(linhaSelecionada, 0); // Assume que o ID da transação está na primeira coluna da tabela

    // Confirmar exclusão com o usuário
    int resposta = JOptionPane.showConfirmDialog(this, "Tem certeza que deseja excluir esta transação?", "Confirmar Exclusão", JOptionPane.YES_NO_OPTION);
    if (resposta == JOptionPane.NO_OPTION) {
        return; // Não excluir, sair do método
    }

    // Excluir a transação do banco de dados
    String sql = "DELETE FROM transacoes WHERE id = ?";
    PreparedStatement pst = null;

    try {
        pst = conexao.prepareStatement(sql);
        pst.setInt(1, idTransacao);

        // Executar a exclusão
        pst.executeUpdate();
        JOptionPane.showMessageDialog(this, "Transação excluída com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);

        // Atualizar a tabela de transações após a exclusão
        atualizarTabelaTransacoes();

        // Atualizar o gráfico de pizza 3D após a exclusão
        telaPrincipal.atualizarTabelaVencimentos();
        telaPrincipal.criarGraficoPizza3D();


    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Erro ao excluir a transação no banco de dados: " + e.getMessage(), "Erro de Banco de Dados", JOptionPane.ERROR_MESSAGE);
        e.printStackTrace(); // Exibir o stack trace no console para depuração
    } finally {
        try {
            if (pst != null) pst.close();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao fechar o PreparedStatement: " + ex.getMessage(), "Erro de Fechamento", JOptionPane.ERROR_MESSAGE);
        }
    }
}

private void configurarCampoValor() {
    // Criar um NumberFormat para formatar valores monetários
    NumberFormat format = NumberFormat.getNumberInstance();
    format.setMinimumFractionDigits(2);  // Garantir 2 casas decimais
    format.setMaximumFractionDigits(2);  // Garantir 2 casas decimais
    NumberFormatter formatter = new NumberFormatter(format);
    formatter.setAllowsInvalid(false); // Impede a entrada de caracteres inválidos

    // Configurar o campo com o formatador
    JFormattedTextField valorField = new JFormattedTextField(formatter);
    valorField.setColumns(10); // Tamanho do campo
    valorField.setValue(0.00); // Definir valor inicial
    valorField.setHorizontalAlignment(JTextField.RIGHT); // Alinhar à direita, como um valor monetário
    txtValor = valorField;
}


private void atualizarTransacao(java.awt.event.ActionEvent evt) {
    // Verificar se uma transação foi selecionada na tabela
    int linhaSelecionada = tblTransacoes.getSelectedRow();
    if (linhaSelecionada == -1) {
        JOptionPane.showMessageDialog(this, "Por favor, selecione uma transação para atualizar.", "Erro de Seleção", JOptionPane.ERROR_MESSAGE);
        return;
    }

    // Obter o ID da transação selecionada
    int idTransacao = (int) tblTransacoes.getValueAt(linhaSelecionada, 0);

    // Capturar os dados do formulário
    String tipoSelecionado = (String) cmbTipos.getSelectedItem();
    String descricaoSelecionada = (String) cmbdescricoes.getSelectedItem();
String valorString = txtValor.getText().replace(",", ".");
double valor;
try {
    valor = ((Number)txtValor.getValue()).doubleValue();  // Obtém diretamente o valor numérico formatado
    if (valor < 0) {
        JOptionPane.showMessageDialog(this, "O valor não pode ser negativo.", "Erro de Valor", JOptionPane.ERROR_MESSAGE);
        return;
    }
} catch (NumberFormatException e) {
    JOptionPane.showMessageDialog(this, "Valor inválido. Certifique-se de usar o formato correto (ex: 50.00).", "Erro no Valor", JOptionPane.ERROR_MESSAGE);
    return;
}

    // Restante do código permanece igual...
    java.sql.Date dataLeitura = (jDateChooser1.getDate() != null) ? new java.sql.Date(jDateChooser1.getDate().getTime()) : null;
    java.sql.Date dataPagamento = (jDateChooser2.getDate() != null) ? new java.sql.Date(jDateChooser2.getDate().getTime()) : null;
    java.sql.Date dataVencimento = (jDateChooser3.getDate() != null) ? new java.sql.Date(jDateChooser3.getDate().getTime()) : null;

    int tipoId = obterTipoId(tipoSelecionado);
    int descricaoId = obterDescricaoId(descricaoSelecionada);
    int statusId = obterStatusId((String) cmbStatus.getSelectedItem());

    // Verificar se os IDs são válidos
    if (tipoId == -1 || descricaoId == -1 || statusId == -1) {
        JOptionPane.showMessageDialog(this, "Erro ao obter as informações selecionadas. Por favor, tente novamente.", "Erro de Seleção", JOptionPane.ERROR_MESSAGE);
        return;
    }

    // Atualizar a transação no banco de dados
    String sql = "UPDATE transacoes SET descricao_id = ?, valor = ?, data_pagamento = ?, proxima_leitura = ?, data_vencimento = ?, status_id = ? WHERE id = ?";
    PreparedStatement pst = null;

    try {
        pst = conexao.prepareStatement(sql);
        pst.setInt(1, descricaoId);
        pst.setDouble(2, valor);
        pst.setDate(3, dataPagamento);
        pst.setDate(4, dataLeitura); 
        pst.setDate(5, dataVencimento);
        pst.setInt(6, statusId);
        pst.setInt(7, idTransacao); // Usar o ID da transação selecionada para atualizar

        // Executar a atualização
        int rowsUpdated = pst.executeUpdate();
        
        if (rowsUpdated > 0) {
            JOptionPane.showMessageDialog(this, "Transação atualizada com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            atualizarTabelaTransacoes(); // Atualiza a tabela de transações após a atualização
            telaPrincipal.atualizarTabelaVencimentos(); // Atualiza tabela de vencimentos
            telaPrincipal.criarGraficoPizza3D(); // Atualiza gráfico
        } else {
            JOptionPane.showMessageDialog(this, "Nenhuma transação foi atualizada. Verifique os dados e tente novamente.", "Erro de Atualização", JOptionPane.ERROR_MESSAGE);
        }

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Erro ao atualizar a transação no banco de dados: " + e.getMessage(), "Erro de Banco de Dados", JOptionPane.ERROR_MESSAGE);
        e.printStackTrace(); // Exibir o stack trace no console para depuração
    } finally {
        try {
            if (pst != null) pst.close();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao fechar o PreparedStatement: " + ex.getMessage(), "Erro de Fechamento", JOptionPane.ERROR_MESSAGE);
        }
    }
}








    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        cmbTipos = new javax.swing.JComboBox<>();
        jLabel2 = new javax.swing.JLabel();
        cmbdescricoes = new javax.swing.JComboBox<>();
        jLabel3 = new javax.swing.JLabel();
        cmbStatus = new javax.swing.JComboBox<>();
        jLabel4 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jDateChooser1 = new com.toedter.calendar.JDateChooser();
        jCheckBox1 = new javax.swing.JCheckBox();
        jLabel7 = new javax.swing.JLabel();
        btnAdicionarTransacoes = new javax.swing.JButton();
        btnExcluirTransacao = new javax.swing.JButton();
        btnAtualizar = new javax.swing.JButton();
        btnLimpar = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblTransacoes = new javax.swing.JTable();
        jDateChooser2 = new com.toedter.calendar.JDateChooser();
        jPanel2 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox<>();
        jPanel3 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        jDateChooser3 = new com.toedter.calendar.JDateChooser();
        jCheckBox2 = new javax.swing.JCheckBox();
        txtValor = new javax.swing.JFormattedTextField();

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setTitle("Tela Transações");

        jLabel1.setText("*Tipos");

        jLabel2.setText("*Descrições");

        jLabel3.setText("*Status");

        jLabel4.setText("*Valor");

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel5.setText("Registrat a Proxima Leitura. Exemplos. Conta de Luz, Água");

        jLabel6.setText("Proxima leitura");

        jDateChooser1.setEnabled(false);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jCheckBox1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel5))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jDateChooser1, javax.swing.GroupLayout.PREFERRED_SIZE, 158, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(20, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jCheckBox1, javax.swing.GroupLayout.Alignment.TRAILING))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel6)
                    .addComponent(jDateChooser1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jLabel7.setText("Data do pagamento");

        btnAdicionarTransacoes.setText("Adicionar");
        btnAdicionarTransacoes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAdicionarTransacoesActionPerformed(evt);
            }
        });

        btnExcluirTransacao.setText("Excluir");
        btnExcluirTransacao.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExcluirTransacaoActionPerformed(evt);
            }
        });

        btnAtualizar.setText("Atualizar");
        btnAtualizar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAtualizarActionPerformed(evt);
            }
        });

        btnLimpar.setText("Limpar");
        btnLimpar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLimparActionPerformed(evt);
            }
        });

        tblTransacoes.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Tipo", "Descrição", "Valor", "Data de Vencimento", "Status", "Data do Pagamento", "Próxima Leitura"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblTransacoes.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblTransacoesMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblTransacoes);

        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel8.setText("Filtro");

        jComboBox1.setToolTipText("");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addComponent(jLabel8)
                .addGap(18, 18, 18)
                .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(28, Short.MAX_VALUE))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel9.setText("Data do Vencimento");

        jDateChooser3.setEnabled(false);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jDateChooser3, javax.swing.GroupLayout.PREFERRED_SIZE, 159, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jCheckBox2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel9)
                        .addGap(0, 46, Short.MAX_VALUE)))
                .addContainerGap(21, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel9)
                    .addComponent(jCheckBox2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jDateChooser3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(20, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel7)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jDateChooser2, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(28, 28, 28)
                                .addComponent(btnAdicionarTransacoes)
                                .addGap(32, 32, 32)
                                .addComponent(btnExcluirTransacao)
                                .addGap(40, 40, 40)
                                .addComponent(btnAtualizar)
                                .addGap(38, 38, 38)
                                .addComponent(btnLimpar)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addGap(104, 104, 104)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(cmbdescricoes, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(47, 47, 47)
                                        .addComponent(jLabel1)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(cmbTipos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(39, 39, 39)
                                        .addComponent(jLabel4)
                                        .addGap(18, 18, 18)
                                        .addComponent(txtValor, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(61, 61, 61)
                                        .addComponent(jLabel3)
                                        .addGap(18, 18, 18)
                                        .addComponent(cmbStatus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(0, 198, Short.MAX_VALUE)))
                        .addGap(18, 18, 18)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(349, 349, 349))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(37, 37, 37)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(8, 8, 8)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(cmbTipos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(cmbdescricoes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(30, 30, 30)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel3)
                                .addComponent(cmbStatus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel4))))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(37, 37, 37)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(11, 11, 11)
                        .addComponent(txtValor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(29, 29, 29)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(16, 16, 16)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel7)
                    .addComponent(jDateChooser2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(31, 31, 31)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAdicionarTransacoes)
                    .addComponent(btnExcluirTransacao)
                    .addComponent(btnAtualizar)
                    .addComponent(btnLimpar))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 397, Short.MAX_VALUE)
                .addContainerGap())
        );

        setBounds(0, 0, 1033, 805);
    }// </editor-fold>//GEN-END:initComponents

    private void tblTransacoesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblTransacoesMouseClicked
if (tblTransacoes.getSelectedRow() != -1) {
    try {
        // Obtém o índice da linha selecionada
        int row = tblTransacoes.getSelectedRow();

        // Recupera os dados da linha selecionada
        int id = (int) tblTransacoes.getValueAt(row, 0);                   // ID da transação (oculto)
        String tipo = (String) tblTransacoes.getValueAt(row, 1);           // Tipo
        String descricao = (String) tblTransacoes.getValueAt(row, 2);      // Descrição
        double valor = (double) tblTransacoes.getValueAt(row, 3);          // Valor
        java.sql.Date dataVencimento = (java.sql.Date) tblTransacoes.getValueAt(row, 4); // Data de Vencimento
        String status = (String) tblTransacoes.getValueAt(row, 5);         // Status
        java.sql.Date dataPagamento = (java.sql.Date) tblTransacoes.getValueAt(row, 6); // Data de Pagamento
        java.sql.Date proximaLeitura = (java.sql.Date) tblTransacoes.getValueAt(row, 7); // Próxima Leitura

        // Preenchendo os campos do formulário com os valores recuperados
        cmbTipos.setSelectedItem(tipo);                       // Seleciona o tipo no JComboBox
        cmbdescricoes.setSelectedItem(descricao);             // Seleciona a descrição no JComboBox
        txtValor.setValue(valor);                             // Define o valor no JFormattedTextField
        cmbStatus.setSelectedItem(status);                    // Seleciona o status no JComboBox

        // Preenche jDateChooser para Data de Vencimento
        if (dataVencimento != null) {
            jDateChooser3.setDate(dataVencimento);           // Define a data de vencimento
        } else {
            jDateChooser3.setDate(null);                     // Limpa o campo se não houver data
        }

        // Preenche jDateChooser para Data de Pagamento
        if (dataPagamento != null) {
            jDateChooser2.setDate(dataPagamento);            // Define a data de pagamento
        } else {
            jDateChooser2.setDate(null);                     // Limpa o campo se não houver data
        }

        // Preenche jDateChooser para Próxima Leitura
        if (proximaLeitura != null) {
            jDateChooser1.setDate(proximaLeitura);           // Define a próxima leitura
        } else {
            jDateChooser1.setDate(null);                     // Limpa o campo se não houver data
        }

    } catch (ClassCastException e) {
        JOptionPane.showMessageDialog(null, "Erro ao recuperar dados da tabela: " + e.getMessage());
    } catch (Exception e) {
        JOptionPane.showMessageDialog(null, "Ocorreu um erro: " + e.getMessage());
    }
} else {
    JOptionPane.showMessageDialog(null, "Nenhuma linha selecionada.");
}

    }//GEN-LAST:event_tblTransacoesMouseClicked

    private void btnAdicionarTransacoesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAdicionarTransacoesActionPerformed
        // Configuração do botão Adicionar Transações
        adicionarTransacao(evt);
    }//GEN-LAST:event_btnAdicionarTransacoesActionPerformed

    private void btnExcluirTransacaoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExcluirTransacaoActionPerformed
        excluirTransacao(evt); // Chama o método que lida com a exclusão da transação
    }//GEN-LAST:event_btnExcluirTransacaoActionPerformed

    private void btnAtualizarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAtualizarActionPerformed
        atualizarTransacao(evt); // Chama o método que lida com a atualização da transação
    }//GEN-LAST:event_btnAtualizarActionPerformed

    private void btnLimparActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimparActionPerformed
        // TODO add your handling code here:
        limparCampos();
    }//GEN-LAST:event_btnLimparActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdicionarTransacoes;
    private javax.swing.JButton btnAtualizar;
    private javax.swing.JButton btnExcluirTransacao;
    private javax.swing.JButton btnLimpar;
    private javax.swing.JComboBox<String> cmbStatus;
    private javax.swing.JComboBox<String> cmbTipos;
    private javax.swing.JComboBox<String> cmbdescricoes;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JComboBox<String> jComboBox1;
    private com.toedter.calendar.JDateChooser jDateChooser1;
    private com.toedter.calendar.JDateChooser jDateChooser2;
    private com.toedter.calendar.JDateChooser jDateChooser3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tblTransacoes;
    private javax.swing.JFormattedTextField txtValor;
    // End of variables declaration//GEN-END:variables
}
