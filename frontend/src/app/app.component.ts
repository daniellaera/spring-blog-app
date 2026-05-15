import { Component, OnInit } from '@angular/core';
import { Router, RouterLink, RouterOutlet } from '@angular/router';
import { LoginService, UserSession } from './login.service';
import { ToolbarModule } from 'primeng/toolbar';
import { ButtonModule } from 'primeng/button';
import { AvatarModule } from 'primeng/avatar';
import { MenuModule } from 'primeng/menu';
import { ToastModule } from 'primeng/toast';
import { MenuItem, MessageService } from 'primeng/api';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, ToolbarModule, ButtonModule, AvatarModule, MenuModule, ToastModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent implements OnInit {
  session: UserSession | null = null;
  userMenuItems: MenuItem[] = [];

  constructor(
    private loginService: LoginService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loginService.getSession().subscribe({
      next: s => {
        this.session = s;
        this.buildMenu();
      },
      error: () => { this.session = null; }
    });
  }

  get userInitial(): string {
    return this.session?.username?.charAt(0).toUpperCase() || '?';
  }

  buildMenu(): void {
    const username = this.session?.username || '';
    this.userMenuItems = [
      { label: username, disabled: true, styleClass: 'font-semibold' },
      { separator: true },
      { label: 'Passkeys & Security', icon: 'pi pi-key', routerLink: '/account/security' },
      { separator: true },
      { label: 'Sign out', icon: 'pi pi-sign-out', command: () => this.logout() }
    ];
  }

  logout(): void {
    this.session = null;
    this.router.navigate(['/login']);
  }
}
