# Video Streaming Android App

## Descripción

Esta aplicación móvil, desarrollada con Android Studio, captura video en tiempo real desde la cámara del celular y lo envía al servidor en formato de imagen (bitmap). Está diseñada para transmitir flujos de video, lo que permite a los usuarios enviar datos en vivo para su posterior procesamiento o análisis en un servidor remoto. El propósito principal de la app es permitir la captura de video de alta calidad y su transmisión en tiempo real a través de la red utilizando un formato compatible con la mayoría de servidores web.

## Características

- **Captura de video en tiempo real**: Utiliza la cámara del dispositivo para capturar video continuo.
- **Envía datos de video al servidor**: Convierte cada frame del video en un bitmap y lo envía al servidor mediante una conexión de red (usando HTTP o UDP según la configuración).
- **Interfaz de usuario simple**: La app ofrece una interfaz sencilla que permite iniciar y detener la captura de video con un solo toque.
- **Compatibilidad con servidores remotos**: Permite la integración con servidores backend que pueden procesar los datos del video, como aplicaciones de visión por computadora o análisis de imágenes.

## Requisitos

- **Android Studio**: La aplicación está desarrollada con Android Studio, por lo que necesitarás tener instalado el entorno de desarrollo.
- **Android 5.0 Lollipop (API 21)** o superior: Se requiere una versión mínima de Android para compatibilidad con las bibliotecas y funcionalidades usadas.
- **Acceso a la cámara**: La aplicación necesita permisos para acceder a la cámara del dispositivo.
- **Conexión a red**: Para enviar los frames al servidor en tiempo real, el dispositivo debe estar conectado a una red Wi-Fi o de datos móviles.

## Funcionalidad

La aplicación realiza las siguientes tareas principales:

1. **Captura de Video**: Utiliza la cámara frontal o trasera del dispositivo para capturar video en tiempo real.
2. **Conversión a Bitmap**: Cada frame capturado se convierte en un objeto Bitmap para ser enviado de forma eficiente al servidor.
3. **Envío al Servidor**: La aplicación utiliza protocolos HTTP o UDP para enviar los datos de los frames al servidor remoto en formato de imagen.
4. **Interfaz de usuario**: La interfaz permite controlar el inicio y detención del video en tiempo real.
