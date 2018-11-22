/*
 * Copyright 2017-present, Nam Seob Seo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { MatButtonModule, MatIconModule, MatToolbarModule } from '@angular/material';
import { HttpClientModule } from '@angular/common/http';

import { DropFileZoneModule } from './drop-file-zone';

import { svgImageConverterInjectables } from './svg-converter/svg-image-converter.injectables';

import { AppComponent } from './app.component';
import { ReadModePipe } from './drop-file-zone/read-mode.pipe';
import { MainMenuComponent } from './main-menu/main-menu.component';
import { SvgConverterComponent } from './svg-converter/svg-converter.component';
import { PortfolioExamplesComponent } from './portfolio-examples/portfolio-examples.component';
import { AboutComponent } from './about/about.component';
import { BottomFooterComponent } from './bottom-footer/bottom-footer.component';
import { SvgImageDropZoneComponent } from './svg-image-drop-zone/svg-image-drop-zone.component';
import { SvgConverterComponentComponent } from './svg-converter-component/svg-converter-component.component';
import { AdvertisementTopComponent } from './advertisement-top/advertisement-top.component';
import { AdvertisementBottomComponent } from './advertisement-bottom/advertisement-bottom.component';

@NgModule({
  declarations: [
    AppComponent,
    ReadModePipe,
    MainMenuComponent,
    SvgConverterComponent,
    PortfolioExamplesComponent,
    AboutComponent,
    BottomFooterComponent,
    SvgImageDropZoneComponent,
    SvgConverterComponentComponent,
    AdvertisementTopComponent,
    AdvertisementBottomComponent
  ],
  imports: [
    NgbModule.forRoot(),
    BrowserModule,
    BrowserAnimationsModule,
    MatButtonModule,
    MatIconModule,
    MatToolbarModule,
    HttpClientModule,
    DropFileZoneModule
  ],
  providers: [
    svgImageConverterInjectables
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
