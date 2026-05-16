import { TestBed, fakeAsync, tick } from '@angular/core/testing';
import { AppComponent } from './app.component';
import { LoginService } from './core/services/login.service';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter, Router } from '@angular/router';
import { MessageService } from 'primeng/api';
import { signal } from '@angular/core';
import { of } from 'rxjs';

function makeLoginService(loggedIn: boolean, user: string | null) {
  return {
    isLoggedIn: signal(loggedIn).asReadonly(),
    currentUser: signal<string | null>(user).asReadonly(),
    getSession: jasmine.createSpy('getSession').and.returnValue(of(null as any)),
    setSession: jasmine.createSpy('setSession'),
    logout: jasmine.createSpy('logout'),
  };
}

describe('AppComponent', () => {
  async function setup(loggedIn = false, user: string | null = null) {
    const loginService = makeLoginService(loggedIn, user);
    await TestBed.configureTestingModule({
      imports: [AppComponent, NoopAnimationsModule],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        MessageService,
        { provide: LoginService, useValue: loginService },
      ]
    }).compileComponents();
    const fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();
    return { fixture, component: fixture.componentInstance, loginService };
  }

  it('should create the app', async () => {
    const { component } = await setup();
    expect(component).toBeTruthy();
  });

  it('should show sign in button when not logged in', async () => {
    const { fixture } = await setup(false, null);
    await fixture.whenStable();
    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('Sign in');
  });

  it('should show register button when not logged in', async () => {
    const { fixture } = await setup(false, null);
    await fixture.whenStable();
    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('Get started');
  });

  it('should show new post button when logged in', async () => {
    const { fixture } = await setup(true, 'alice');
    await fixture.whenStable();
    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('New Post');
  });

  it('userInitial computed returns uppercased first char of current user', async () => {
    const { component } = await setup(true, 'alice');
    expect(component.userInitial()).toBe('A');
  });

  it('userInitial returns ? when no user', async () => {
    const { component } = await setup(false, null);
    expect(component.userInitial()).toBe('?');
  });

  it('logout calls loginService.logout and navigates to /', async () => {
    const { component, loginService } = await setup(true, 'alice');
    const router = TestBed.inject(Router);
    spyOn(router, 'navigate').and.returnValue(Promise.resolve(true));
    component.logout();
    expect(loginService.logout).toHaveBeenCalled();
    expect(router.navigate).toHaveBeenCalledWith(['/']);
  });

  it('should call loginService.getSession on init', fakeAsync(async () => {
    const { loginService } = await setup();
    tick(5000);
    expect(loginService.getSession).toHaveBeenCalled();
  }));
});
