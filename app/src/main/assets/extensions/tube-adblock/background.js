"use strict";

const BLOCKED_HOSTS = [
    "doubleclick.net",
    "googlesyndication.com",
    "googleadservices.com",
    "adservice.google.com",
    "pagead.googlesyndication.com"
];

browser.webRequest.onBeforeRequest.addListener(
    function(details) {

        const url = details.url.toLowerCase();

        for (const host of BLOCKED_HOSTS) {
            if (url.includes(host)) {
                return {
                    cancel: true
                };
            }
        }

        return {};
    },
    {
        urls: ["*://*/*"]
    },
    ["blocking"]
);
