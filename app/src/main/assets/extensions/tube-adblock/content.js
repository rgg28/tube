"use strict";

(function () {

    function hideAds() {

        const selectors = [
            "ytd-display-ad-renderer",
            "ytd-promoted-video-renderer",
            "#player-ads",
            ".ytp-ad-overlay-container",
            ".ytp-ad-text",
            ".ytp-ad-image-overlay",
            ".ytp-ad-message-container",
            ".ytp-ad-player-overlay",
            ".ytp-ad-module",
            "ytd-ad-slot-renderer",
            "ytd-in-feed-ad-layout-renderer",
            "ytd-banner-promo-renderer"
        ];

        selectors.forEach(selector => {

            document
                .querySelectorAll(selector)
                .forEach(element => {

                    element.style.setProperty(
                        "display",
                        "none",
                        "important"
                    );

                    element.style.setProperty(
                        "visibility",
                        "hidden",
                        "important"
                    );
                });
        });
    }

    function processVideo() {

        const video = document.querySelector("video");

        if (!video) {
            return;
        }

        const ad = document.querySelector(".ad-showing");

        if (!ad) {
            return;
        }

        try {
            video.muted = true;
            video.playbackRate = 16.0;
        } catch (e) {
        }
    }

    function skipAd() {

        const selectors = [
            ".ytp-ad-skip-button",
            ".ytp-skip-ad-button",
            ".ytp-ad-skip-button-modern"
        ];

        for (const selector of selectors) {

            const button =
                document.querySelector(selector);

            if (button) {

                try {
                    button.click();
                } catch (e) {
                }
            }
        }
    }

    function run() {

        hideAds();
        skipAd();
        processVideo();
    }

    function start() {

        run();

        setInterval(
            run,
            300
        );

        const observer =
            new MutationObserver(run);

        if (document.documentElement) {

            observer.observe(
                document.documentElement,
                {
                    childList: true,
                    subtree: true
                }
            );
        }
    }

    if (document.readyState === "loading") {

        document.addEventListener(
            "DOMContentLoaded",
            start,
            {
                once: true
            }
        );

    } else {

        start();
    }

})();
