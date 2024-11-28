![GitHub](https://img.shields.io/github/license/professorjosedeassis/infoX)

# ☕ Sistema de Controle Financeiro
O **Sistema de Controle Financeiro** é uma aplicação desktop para gerenciar receitas e despesas, com uma interface amigável e gráficos interativos para facilitar a análise financeira. O sistema também conta com funcionalidades de backup e restauração para garantir a segurança dos dados e oferece controle de usuários e permissões.

![Sistema Controle Financeiro](https://github.com/alburilli/Sistema-de-Controle-Financeiro/blob/main/assets/Screenshot_1.png)

## Autor
Anderson Luiz

---

## Funcionalidades

- Cadastro de **receitas** e **despesas**.
- Exibição de **gráficos** de análise financeira.
- Funcionalidade de **backup** e **restauração** do banco de dados.
- Controle de **usuários** e **permissões**.

---

## Instruções para instalação e uso do aplicativo
### Pré requisitos
1) Ter o Java **versão 8** instalado.

[download Java 8](https://www.java.com/pt-BR/)

2) O NetBeans IDE permite o desenvolvimento rápido e fácil de aplicações desktop Java, móveis e Web, oferecendo excelentes ferramentas para desenvolvedores.

[NetBeans IDE 8.2](https://filehippo.com/download_netbeans/8.2/)

3) Ter um banco de dados local baseado no **MySQL 8** ou MariaDB compatível, no exemplo usei o XAMPP que pode ser obtido no link indicado.

[download xampp](https://www.apachefriends.org/)

4) O MySQL fornece drivers baseados em padrões para JDBC, ODBC e .Net, permitindo que os desenvolvedores criem aplicativos de banco de dados na linguagem de sua escolha.

[driver MySQL](https://dev.mysql.com/downloads/connector/j/)

5) JFreeChart é uma biblioteca de gráficos 100% Java gratuita que facilita para os desenvolvedores exibir gráficos de qualidade profissional em seus aplicativos.

[download jfreechart](https://sourceforge.net/projects/jfreechart/files/1.%20JFreeChart/1.0.19/)

6) JCalendar é um bean seletor de data Java para escolher uma data graficamente.

[download jcalendar](https://toedter.com/jcalendar/)



### Tecnologias Utilizadas

- **Linguagem**: Java
- **IDE**: NetBeans 8.2.
- **Banco de Dados**: MySQL (recomendado XAMPP).
- **Bibliotecas/Frameworks**: Swing, JFreeChart, jcalendar-1.4 jfreechart 1.0.19

---

### Instalação do Banco de Dados

1. **Instalar XAMPP**:
   - Baixe o XAMPP [aqui](https://www.apachefriends.org/).
   - Instale e inicie os serviços **Apache** e **MySQL**.

   ![XAMPP Start](https://github.com/alburilli/Sistema-de-Controle-Financeiro/blob/main/assets/Screenshot_2.png)

2. **Acessar o phpMyAdmin**:
   - Abra seu navegador e acesse: `localhost/dashboard`, então selecione **phpMyAdmin**.

   ![phpMyAdmin](https://github.com/alburilli/Sistema-de-Controle-Financeiro/blob/main/assets/Screenshot_3.png)

3. **Criar o Banco de Dados**:
   - Crie o banco de dados **dbfinanceiro** no phpMyAdmin.
   
   ![dbfinanceiro](https://github.com/alburilli/Sistema-de-Controle-Financeiro/blob/main/assets/Screenshot_4.png)

4. **Criar as Tabelas**:
   - Na aba **SQL**, cole o seguinte código e execute:

   ```sql
   CREATE DATABASE dbfinanceiro;
   USE dbfinanceiro;

   -- Tabela de Usuários
   CREATE TABLE tbusuarios (
       iduser INT PRIMARY KEY AUTO_INCREMENT,  
       usuario VARCHAR(50) NOT NULL,         
       fone VARCHAR(20),                    
       login VARCHAR(50) NOT NULL UNIQUE,  
       senha VARCHAR(250) NOT NULL,        
       perfil VARCHAR(20) NOT NULL        
   );

   -- Tabela de Tipos de Transações
   CREATE TABLE tipos (
       id INT PRIMARY KEY AUTO_INCREMENT,      
       nome VARCHAR(20) NOT NULL UNIQUE       
   );

   -- Tabela de Status de Transações
   CREATE TABLE status_transacoes (
       id INT PRIMARY KEY AUTO_INCREMENT,       
       descricao VARCHAR(50) NOT NULL UNIQUE    
   );

   -- Tabela de Descrições
   CREATE TABLE descricoes (
       id INT PRIMARY KEY AUTO_INCREMENT,       
       descricao VARCHAR(255) NOT NULL UNIQUE,  
       tipo_id INT NOT NULL,                    
       data_vencimento DATE,                    
       FOREIGN KEY (tipo_id) REFERENCES tipos(id) ON DELETE RESTRICT
   );

   -- Tabela de Transações
   CREATE TABLE transacoes (
       id INT PRIMARY KEY AUTO_INCREMENT,        
       descricao_id INT NOT NULL,                
       valor DECIMAL(10, 2) NOT NULL,            
       data_pagamento DATE DEFAULT NULL,         
       proxima_leitura DATE DEFAULT NULL,        
       data_vencimento DATE DEFAULT NULL,        
       data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
       ultima_atualizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
       status_id INT,                            
       FOREIGN KEY (descricao_id) REFERENCES descricoes(id) ON DELETE RESTRICT,
       FOREIGN KEY (status_id) REFERENCES status_transacoes(id) ON DELETE RESTRICT
   );

   -- Inserindo dados de exemplo
   INSERT INTO tbusuarios (usuario, fone, login, senha, perfil) VALUES 
   ('Administrador', '123456789', 'admin', 'admin', 'admin'),
   ('Usuario', '987654321', 'user', 'user1', 'user');

   INSERT INTO tipos (nome) VALUES
   ('Receita'),
   ('Despesa');

   INSERT INTO status_transacoes (descricao) VALUES
   ('Pago'),
   ('Pendente');