import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { PostListComponent } from './post-list.component';
import { PostService } from '../../../core/services/post.service';
import { LoginService } from '../../../core/services/login.service';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { provideRouter } from '@angular/router';
import { of, throwError, delay } from 'rxjs';
import { PostDTO } from '../../../dto/post.dto';
import { signal } from '@angular/core';

const mockPosts: PostDTO[] = [
  { id: 1, title: 'Post One', content: 'Content 1', author: 'alice', comments: [] },
  { id: 2, title: 'Post Two', content: 'Content 2', author: 'bob', comments: [] },
];

describe('PostListComponent', () => {
  let component: PostListComponent;
  let fixture: ComponentFixture<PostListComponent>;
  let postServiceSpy: jasmine.SpyObj<PostService>;

  function makeLoginService(loggedIn = false): Partial<LoginService> {
    return {
      isLoggedIn: signal(loggedIn).asReadonly(),
      currentUser: signal<string | null>(null).asReadonly(),
    };
  }

  async function setup(posts$: any, loggedIn = false) {
    postServiceSpy = jasmine.createSpyObj('PostService', ['getAll', 'delete']);
    postServiceSpy.getAll.and.returnValue(posts$);

    await TestBed.configureTestingModule({
      imports: [PostListComponent, NoopAnimationsModule],
      providers: [
        provideRouter([]),
        { provide: PostService, useValue: postServiceSpy },
        { provide: LoginService, useValue: makeLoginService(loggedIn) },
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(PostListComponent);
    component = fixture.componentInstance;
  }

  it('should create', fakeAsync(async () => {
    await setup(of([]));
    fixture.detectChanges();
    tick();
    expect(component).toBeTruthy();
  }));

  it('should set loading to true initially before data arrives', async () => {
    await setup(of(mockPosts).pipe(delay(100)));
    fixture.detectChanges();
    expect(component.loading).toBeTrue();
  });

  it('should set loading to false after posts load', fakeAsync(async () => {
    await setup(of(mockPosts));
    fixture.detectChanges();
    tick();
    expect(component.loading).toBeFalse();
  }));

  it('should display posts after load', fakeAsync(async () => {
    await setup(of(mockPosts));
    fixture.detectChanges();
    tick();
    expect(component.posts.length).toBe(2);
    expect(component.posts[0].title).toBe('Post One');
  }));

  it('should show empty array when no posts returned', fakeAsync(async () => {
    await setup(of([]));
    fixture.detectChanges();
    tick();
    expect(component.posts).toEqual([]);
    expect(component.loading).toBeFalse();
  }));

  it('should set loading to false and posts to empty on HTTP error', fakeAsync(async () => {
    await setup(throwError(() => new Error('Network error')));
    fixture.detectChanges();
    tick();
    expect(component.posts).toEqual([]);
    expect(component.loading).toBeFalse();
  }));

  it('should call postService.getAll on init', fakeAsync(async () => {
    await setup(of([]));
    fixture.detectChanges();
    tick();
    expect(postServiceSpy.getAll).toHaveBeenCalledTimes(1);
  }));

  it('should append empty comments array to posts with undefined comments', fakeAsync(async () => {
    const postsWithoutComments: PostDTO[] = [
      { id: 1, title: 'Post', content: 'Content', author: 'alice', comments: undefined as any },
    ];
    await setup(of(postsWithoutComments));
    fixture.detectChanges();
    tick();
    expect(component.posts[0].comments).toEqual([]);
  }));
});
