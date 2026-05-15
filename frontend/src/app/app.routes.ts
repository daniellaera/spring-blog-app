import { Routes } from '@angular/router';
import { PostListComponent } from './features/posts/post-list/post-list.component';
import { PostDetailComponent } from './features/posts/post-detail/post-detail.component';
import { UserComponent } from './features/user/user.component';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  { path: '', component: PostListComponent },
  { path: 'dashboard', redirectTo: '' },
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: 'register',
    loadComponent: () => import('./features/auth/register/register.component').then(m => m.RegisterComponent)
  },
  {
    path: 'posts/new',
    loadComponent: () => import('./features/posts/post-create/post-create.component').then(m => m.PostCreateComponent),
    canActivate: [authGuard]
  },
  { path: 'posts/:postId/edit', redirectTo: '/', pathMatch: 'full' },
  { path: 'posts/:postId', component: PostDetailComponent },
  { path: 'user', component: UserComponent, canActivate: [authGuard] },
  { path: 'my-posts', component: UserComponent, canActivate: [authGuard] },
  {
    path: 'account/security',
    loadComponent: () => import('./account/security/passkey-list.component')
      .then(m => m.PasskeyListComponent),
    canActivate: [authGuard]
  },
  { path: '**', redirectTo: '' }
];
