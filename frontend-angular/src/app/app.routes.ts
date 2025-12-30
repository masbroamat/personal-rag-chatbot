import { Routes } from '@angular/router';
import { ChatComponent } from './components/chat/chat.component';

export const routes: Routes = [
  { 
    path: 'chat/:id', 
    component: ChatComponent 
  },
  { 
    path: '', 
    redirectTo: 'chat/new', 
    pathMatch: 'full' 
  },
  { 
    path: '**', 
    redirectTo: 'chat/new' 
  }
];