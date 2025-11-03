# Check Cadet - CC42

<div align="center">

![Check Cadet Logo](https://img.shields.io/badge/Check%20Cadet-CC42-blue?style=for-the-badge)
![Platform](https://img.shields.io/badge/platform-Android-green?style=for-the-badge&logo=android)
![License](https://img.shields.io/badge/License-CC%20BY--NC--ND%204.0-lightgrey.svg?style=for-the-badge)

**Solução Digital Integrada para Gestão de Presença e Refeições na Escola 42**

</div>

---

## Sobre o Projeto

**Check Cadet** é uma aplicação Android nativa desenvolvida especificamente para a comunidade da Escola 42, proporcionando uma solução completa e digitalizada para gestão de presenças em eventos, subscrições de refeições e localização de estudantes no campus. Através de tecnologias modernas como leitura de **QR Code** e **NFC (Near Field Communication)**, a aplicação oferece uma experiência rápida, eficiente e segura para estudantes e staff.

## Funcionalidades Principais

### Para Estudantes

#### 1. **Gestão de Eventos e Presença**
- Visualização de eventos disponíveis organizados por curso
- Marcação de presença através de QR Code gerado dinamicamente
- Histórico de eventos participados
- Notificações em tempo real sobre novos eventos
- Geração de QR Code pessoal para check-in rápido

#### 2. **Sistema de Refeições**
- Visualização do cardápio diário organizado por tipo de refeição (pequeno-almoço, almoço, jantar)
- Subscrição digital às refeições via QR Code ou NFC
- Suporte para primeira e segunda porção
- Notificações push sobre disponibilidade de refeições
- Sistema de avaliação das refeições (rating)
- Visualização detalhada dos componentes nutricionais:
  - Carboidratos (arroz, massas, funge, batatas, pães)
  - Proteínas, leguminosas e vegetais
- Histórico de refeições subscritas
- Indicador visual de status de subscrição

#### 3. **Localização no Campus**
- Visualização em tempo real da localização de outros estudantes
- Registro manual de localização no campus
- Mapa interativo com overlay visual das localizações
- Sistema de lembretes diários para atualização de localização
- Indicador de confiabilidade baseado na frequência de atualização
- Pesquisa de estudantes por nome ou login

#### 4. **Sistema de Mensagens**
- Recepção de mensagens e comunicados oficiais
- Organização por curso (42Cursus, C Piscine, etc.)
- Histórico de mensagens recebidas

#### 5. **Autenticação e Perfil**
- Login seguro via OAuth 2.0 integrado com a API da 42
- Sincronização automática de dados do perfil
- Integração com sistema de coalitions
- Visualização de informações pessoais e acadêmicas

### Para Staff/Administradores

#### 1. **Gestão de Eventos**
- Criação e edição de eventos
- Geração de QR Codes para eventos
- Visualização da lista completa de participantes
- Marcação de início/encerramento de eventos
- Exportação de listas de presença em PDF e CSV
- Impressão e compartilhamento de relatórios
- Sincronização com Firebase Realtime Database
- Controle de check-in e check-out

#### 2. **Gestão de Refeições**
- Criação e edição de refeições
- Upload de imagens (integração com Cloudinary)
- Configuração de quantidade disponível
- Controle de status (ativo/bloqueado)
- Envio de notificações push sobre refeições
- Visualização de estatísticas de subscrições
- Exportação de listas de subscritos (PDF/CSV)
- Gestão de múltiplos cursos simultaneamente

#### 3. **Gestão de Subscrições**
- Scanner integrado de QR Code (câmera frontal e traseira)
- Leitor NFC para cartões de estudante
- Verificação automática de subscrições duplicadas
- Indicadores visuais de status (subscrito/não subscrito)
- Controle de primeira e segunda porção
- Pesquisa rápida de estudantes
- Scroll infinito para grandes listas

#### 4. **Sistema de Notificações**
- Envio de notificações push via Firebase Cloud Messaging
- Sistema de tópicos por curso e campus
- Notificações de refeições disponíveis
- Alertas de eventos importantes

## Tecnologias Utilizadas

### Core Android
- **Linguagem**: Java
- **SDK mínimo**: API 24 (Android 7.0)
- **Android Jetpack**: Navigation, ViewModel, LiveData, DataBinding
- **Material Design 3**: Components e theming moderno

### Recursos Nativos
- **NFC**: Leitura de tags NDEF e Mifare Classic
- **Câmera**: CameraX para captura e análise
- **Reconhecimento Facial**: TensorFlow Lite (MobileFaceNet)
- **NDK/JNI**: Código nativo em C++ para processamento de imagem

### Backend & Cloud
- **Firebase Realtime Database**: Armazenamento de dados em tempo real
- **Firebase Cloud Messaging**: Sistema de notificações push
- **Cloudinary**: Upload e gestão de imagens
- **API REST**: Integração com backend da Escola 42

### Bibliotecas Principais
- **ZXing**: Geração e leitura de QR Codes
- **Glide**: Carregamento eficiente de imagens
- **iText PDF**: Geração de documentos PDF
- **Retrofit**: Cliente HTTP para APIs REST
- **Gson**: Serialização/deserialização JSON

### Segurança
- **AES Encryption**: Criptografia de dados sensíveis
- **OAuth 2.0**: Autenticação segura
- **ProGuard**: Ofuscação de código

## Interface e Experiência do Usuário

- **Design Moderno**: Interface seguindo Material Design 3
- **Modo Escuro**: Suporte completo a tema claro e escuro
- **Multilíngue**: Suporte para Português, Inglês, Francês e Espanhol
- **Personalização**: Cores dinâmicas baseadas em coalitions
- **Performance**: Scroll infinito e carregamento progressivo
- **Sincronização**: Dados em tempo real via Firebase
- **Atalhos**: App shortcuts para acesso rápido às funcionalidades

## Segurança e Privacidade

- Autenticação via OAuth 2.0
- Criptografia AES para dados sensíveis
- Validação de QR Codes com timestamp
- Controle de permissões por tipo de usuário (estudante/staff)
- Comunicação segura HTTPS
- Armazenamento local criptografado (SharedPreferences)

## Arquitetura

O projeto segue o padrão **MVVM (Model-View-ViewModel)** com:
- **Repository Pattern**: Abstração de fontes de dados
- **LiveData**: Observação reativa de mudanças
- **ViewModel**: Gerenciamento de estado da UI
- **Data Binding**: Vinculação declarativa de dados
- **Navigation Component**: Navegação entre telas

## Funcionalidades Técnicas Avançadas

- **Firebase Integration**: Database, Messaging e Analytics
- **Computer Vision**: Reconhecimento facial com TensorFlow Lite
- **NFC Technology**: Leitura de múltiplos formatos de tags
- **PDF Generation**: Criação dinâmica de documentos com iText
- **Background Tasks**: WorkManager para tarefas assíncronas
- **Push Notifications**: Sistema completo de notificações
- **Offline Support**: Cache local e sincronização automática
- **Camera Processing**: Processamento de imagem em C++ (JNI)

## Estrutura do Projeto

```
android-cc/
├── app/
│   ├── src/main/
│   │   ├── cpp/                    # Código nativo C++
│   │   ├── java/com/antonioteca/
│   │   │   ├── dao/               # Data Access Objects
│   │   │   ├── factory/           # ViewModelFactories
│   │   │   ├── facerecognition/   # Reconhecimento facial
│   │   │   ├── model/             # Modelos de dados
│   │   │   ├── network/           # Networking e APIs
│   │   │   ├── repository/        # Repositórios
│   │   │   ├── ui/                # Interface do usuário
│   │   │   │   ├── event/         # Gestão de eventos
│   │   │   │   ├── meal/          # Gestão de refeições
│   │   │   │   ├── location/      # Localização
│   │   │   │   ├── home/          # Tela inicial
│   │   │   │   └── ...
│   │   │   ├── utility/           # Utilitários
│   │   │   └── viewmodel/         # ViewModels
│   │   ├── res/                   # Recursos Android
│   │   └── assets/                # Assets (TFLite models)
└── gradle/                        # Configurações Gradle
```

## Casos de Uso

### Estudante Comum:
1. Fazer login com credenciais da 42
2. Visualizar eventos disponíveis
3. Gerar QR Code pessoal
4. Marcar presença em eventos
5. Subscrever refeições
6. Atualizar localização no campus
7. Receber notificações de refeições

### Staff/Administrador:
1. Criar novos eventos
2. Gerar QR Code do evento
3. Escanear QR Codes de estudantes
4. Exportar lista de presença
5. Criar e gerir refeições
6. Enviar notificações
7. Visualizar estatísticas

## Diferenciais

- **Multiplataforma de Identificação**: QR Code + NFC + Reconhecimento Facial
- **Offline First**: Funciona sem conexão com sincronização posterior
- **Tempo Real**: Atualizações instantâneas via Firebase
- **Escalável**: Suporte para múltiplos campus e cursos
- **Multilíngue**: Interface em 4 idiomas
- **Acessível**: Design inclusivo e responsivo

## Licença

Este projeto **Check Cadet - CC42** © 2024 está licenciado sob a licença [Creative Commons Atribuição-NãoComercial-SemDerivações 4.0 Internacional (CC BY-NC-ND 4.0)](https://creativecommons.org/licenses/by-nc-nd/4.0/).

![Licença CC BY-NC-ND 4.0](https://licensebuttons.net/l/by-nc-nd/4.0/88x31.png)

### Restrições:
- ❌ **Uso Comercial Proibido**: Não pode ser usado para fins comerciais
- ❌ **Sem Derivações**: Não pode criar obras derivadas
- **Atribuição Obrigatória**: Deve dar crédito apropriado ao autor

---

<div align="center">

**Desenvolvido especialmente para a comunidade 42 Luanda**

*Check Cadet - Tornando a gestão acadêmica mais eficiente e digital*

</div>