import { Component, Input } from '@angular/core';
import { Book } from '../classes/book';
import { TradingOption } from 'src/app/elements/enums/trading-option';
import { KeyValuePipe } from '@angular/common';
import { keyframes } from '@angular/animations';

@Component({
  selector: 'app-book-preview',
  templateUrl: './book-preview.component.html',
  styleUrls: ['./book-preview.component.css']
})
export class BookPreviewComponent {
  @Input() book!: Book;
  bookImageIndex: number = 0;
  tradingOptions: string[] = [];

  ngOnInit()
  {
    if(this.book.tradingOptions.includes(TradingOption.ALL))
    {
      this.tradingOptions.push(TradingOption.CURRENCY);
      this.tradingOptions.push(TradingOption.POINTS);
      this.tradingOptions.push(TradingOption.SWAP);
    } else {
      this.tradingOptions = this.book.tradingOptions;
    }
  }

  public printTradingOption(option: any): string
  {
    return option.charAt(0) + option.slice(1).toLowerCase();
  }

  nextBookImage(): void
  {
    this.bookImageIndex++;
    this.bookImageIndex %= this.book.images.length;
  }

  previousBookImage(): void
  {
    this.bookImageIndex > 0 ?
      this.bookImageIndex-- :
      this.bookImageIndex = this.book.images.length-1;
  }
}
