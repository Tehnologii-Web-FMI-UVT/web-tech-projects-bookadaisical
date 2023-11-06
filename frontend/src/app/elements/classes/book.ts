import { User } from "../../account-management/user";
import { BookPurpose } from "../../utils/enums/book-purpose";

export class Book {
    uniqueId: number | undefined;
    title: string | undefined;
    author: string | undefined;
    uploader: User | undefined;
    ISBN: string | undefined;
    images: string[] | undefined;
    views: number | undefined;
    purpose: BookPurpose | undefined;
    priceCurrency: number | undefined;
    pricePoints: number | undefined;
    description: Text | undefined;
    created: Date | undefined;
    active: boolean | undefined;
}