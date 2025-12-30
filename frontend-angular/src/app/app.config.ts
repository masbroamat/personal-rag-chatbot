import { ApplicationConfig, provideZoneChangeDetection } from "@angular/core";
import { provideRouter } from "@angular/router";
import { provideHttpClient } from "@angular/common/http";
import { routes } from "./app.routes";
import { provideMarkdown } from "ngx-markdown";
import {
  provideClientHydration,
  withEventReplay,
} from "@angular/platform-browser";

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    // provideClientHydration(withEventReplay()),
    provideHttpClient(),
    provideMarkdown(),
  ],
};
