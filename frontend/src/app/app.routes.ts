import { Routes } from '@angular/router';
import { PostListComponent } from './post-list/post-list.component';
import { PostDetailComponent } from './post-detail/post-detail.component';
import { HomeComponent } from './home/home.component';
import { UserComponent } from './user/user.component';
import { authGuard } from './auth.guard';

export const routes: Routes = [
  { path: '', component: PostListComponent },
  { path: 'dashboard', redirectTo: '' },
  { path: 'login', component: HomeComponent },
  {
    path: 'register',
    loadComponent: () => import('./register/register.component').then(m => m.RegisterComponent)
  },
  { path: 'post/:postId', component: PostDetailComponent },
  { path: 'user', component: UserComponent, canActivate: [authGuard] },
  {
    path: 'account/security',
    loadComponent: () => import('./account/security/passkey-list.component')
      .then(m => m.PasskeyListComponent),
    canActivate: [authGuard]
  },
  { path: '**', redirectTo: '' }
];
