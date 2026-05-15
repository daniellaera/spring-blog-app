import { Component, OnInit, computed, inject } from '@angular/core';
import { Router, RouterLink, RouterOutlet } from '@angular/router';
import { LoginService } from './core/services/login.service';
import { ToolbarModule } from 'primeng/toolbar';
import { ButtonModule } from 'primeng/button';
import { AvatarModule } from 'primeng/avatar';
import { MenuModule } from 'primeng/menu';
import { ToastModule } from 'primeng/toast';
import { MenuItem } from 'primeng/api';
import { timeout, catchError } from 'rxjs/operators';
import { of } from 'rxjs';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, ToolbarModule, ButtonModule, AvatarModule, MenuModule, ToastModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent implements OnInit {
  private loginService = inject(LoginService);
  private router = inject(Router);

  isLoggedIn = this.loginService.isLoggedIn;
  currentUser = this.loginService.currentUser;

  readonly userInitial = computed(() =>
    this.currentUser()?.charAt(0)?.toUpperCase() ?? '?'
  );

  readonly menuItems = computed<MenuItem[]>(() => [
    { label: this.loginService.currentUser() ?? 'Account', disabled: true },
    { separator: true },
    { label: 'Passkeys & Security', icon: 'pi pi-key', routerLink: '/account/security' },
    { separator: true },
    { label: 'Sign out', icon: 'pi pi-sign-out', command: () => this.logout() }
  ]);

  ngOnInit(): void {
    this.loginService.getSession().pipe(
      timeout(5000),
      catchError(() => of(null))
    ).subscribe(session => {
      this.loginService.setSession(session);
    });
  }

  logout(): void {
    this.loginService.logout();
    this.router.navigate(['/']);
  }
}
