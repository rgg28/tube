(function() {
    setInterval(() => {
        // 1. Detectar si hay un video de anuncio activo
        const video = document.querySelector('video');
        const isAd = document.querySelector('.ad-showing, .pns-ad-showing, .ytp-ad-player-overlay');
        
        if (video && isAd) {
            // Truco de magia: Multiplica la velocidad del anuncio x16 para que termine en un parpadeo
            video.playbackRate = 16.0;
            video.muted = true; // Lo silencia para que no escuches ruido molesto
            
            // 2. Hacer clic automáticamente en el botón "Omitir anuncio" si aparece
            const skipButton = document.querySelector('.ytp-ad-skip-button, .ytp-skip-ad-button');
            if (skipButton) {
                skipButton.click();
            }
        }

        // 3. Ocultar banners publicitarios estáticos de la interfaz web
        const adElements = document.querySelectorAll('ytd-display-ad-renderer, ytd-promoted-video-renderer, #player-ads, .ytp-ad-overlay-container');
        adElements.forEach(el => el.style.display = 'none');
    }, 300); // Revisa la pantalla cada 300 milisegundos
})();
